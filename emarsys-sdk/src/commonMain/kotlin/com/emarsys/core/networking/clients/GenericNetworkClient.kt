package com.emarsys.core.networking.clients

import com.emarsys.core.exceptions.SdkException.CoroutineException
import com.emarsys.core.exceptions.SdkException.FailedRequestException
import com.emarsys.core.exceptions.SdkException.NetworkIOException
import com.emarsys.core.exceptions.SdkException.RetryLimitReachedException
import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.retry
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

internal class GenericNetworkClient(
    private val client: HttpClient,
    private val sdkLogger: Logger,
) : NetworkClientApi {

    private companion object {
        const val MAX_RETRY_COUNT = 5
        val RETRY_DELAY = 2.seconds
    }

    override suspend fun send(
        request: UrlRequest
    ): Result<Response> {
        var retries = 0
        var result: Result<Response>
        val networkDuration = measureTime {
            result = try {
                val httpResponse = client.request {
                    val shouldAddDefaultHeader =
                        request.headers?.get(HttpHeaders.ContentType)?.toString().isNullOrEmpty()
                    if (shouldAddDefaultHeader) {
                        contentType(ContentType.Application.Json)
                    }
                    if (request.shouldRetryOnFail) {
                        retry {
                            constantDelay(RETRY_DELAY.inWholeMilliseconds)
                            retryIf(MAX_RETRY_COUNT) { _, httpResponse ->
                                retries++
                                shouldRetry(httpResponse)
                            }
                        }
                    }
                    method = request.method
                    url(request.url)
                    request.headers?.forEach {
                        header(it.key, it.value)
                    }
                    request.bodyString?.let { setBody(request.bodyString) }
                }
                Result.success(
                    Response(
                        request,
                        httpResponse.status,
                        httpResponse.headers,
                        httpResponse.bodyAsText()
                    )
                )
            } catch (throwable: Throwable) {
                sdkLogger.error(
                    "EventClient - consumeEvents: Exception during event consumption",
                    throwable,
                    isRemoteLog = !request.isLogRequest
                )
                Result.failure(
                    if (coroutineContext.isActive) {
                        NetworkIOException(
                            throwable.message ?: "IOException during network request"
                        )
                    } else {
                        CoroutineException(
                            throwable.message ?: "Coroutine cancelled during network request"
                        )
                    }
                )
            }
        }
        val response = result.getOrNull()
        response?.let {
            networkDebugLog(request, it, networkDuration)
            networkInfoLog(request, it, networkDuration, retries)
            result = when {
                !it.status.isSuccess() && it.status != HttpStatusCode.Unauthorized && retries == MAX_RETRY_COUNT -> {
                    networkErrorLog("Request retry limit reached!", it, request, retries, networkDuration)
                    Result.failure(RetryLimitReachedException("Request retry limit reached! Response: ${it.bodyAsText}"))
                }
                else -> {
                    networkErrorLog("Request failed with status code: ${it.status.value}", it, request, retries, networkDuration)
                    Result.failure(FailedRequestException(it))
                }
            }
        }
        return result
    }

    private suspend fun networkInfoLog(
        request: UrlRequest,
        response1: Response,
        networkDuration: Duration,
        retries: Int
    ) {
        sdkLogger.info(
            "log_request",
            buildJsonObject {
                put("url", request.url.toString())
                put("method", request.method.value)
                put("statusCode", response1.status.value)
                put("networkingDuration", networkDuration.inWholeMilliseconds)
                put("retries", retries)
            },
            isRemoteLog = !request.isLogRequest
        )
    }

    private suspend fun networkDebugLog(
        request: UrlRequest,
        response1: Response,
        networkDuration: Duration
    ) {
        sdkLogger.debug(
            "log_request",
            buildJsonObject {
                put("url", request.url.toString())
                put("method", request.method.value)
                put("statusCode", response1.status.value)
                if (request.bodyString != null) {
                    put("payload", request.bodyString)
                }
                if (request.headers != null) {
                    put("header", request.headers.toString())
                }
                put("networkingDuration", networkDuration.inWholeMilliseconds)
            },
            isRemoteLog = !request.isLogRequest
        )
    }

    private suspend fun networkErrorLog(topic: String, response: Response, request: UrlRequest, retries: Int, networkDuration: Duration) {
        sdkLogger.error(
            LogEntry(
                topic,
                buildJsonObject {
                    put("status", response.status.value)
                    put(
                        "networkingDuration",
                        networkDuration.inWholeMilliseconds
                    )
                    put("retries", retries)
                    put("url", response.originalRequest.url.toString())
                }
            ),
            isRemoteLog = !request.isLogRequest
        )
    }

    private fun shouldRetry(response: HttpResponse): Boolean {
        return !response.status.isSuccess() && (response.status.value == 408 || response.status.value == 429 || response.status.value !in 400..499)
    }
}
