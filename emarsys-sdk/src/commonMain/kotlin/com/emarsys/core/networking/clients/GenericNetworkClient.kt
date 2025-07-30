package com.emarsys.core.networking.clients

import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.RetryLimitReachedException
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
import kotlinx.coroutines.ensureActive
import kotlinx.io.IOException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.coroutineContext
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
        request: UrlRequest,
        onNetworkError: (suspend () -> Unit)?
    ): Response {
        var retries = 0
        val httpResponse: HttpResponse
        val networkDuration = measureTime {
            httpResponse = try {
                client.request {
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
            } catch (exception: IOException) {
                onNetworkError?.invoke()
                sdkLogger.debug(
                    "EventClient - consumeEvents: IOException during event consumption (iOS, Android)",
                    exception,
                    isRemoteLog = !request.isLogRequest
                )
                throw exception
            } catch (exception: Exception) {
                coroutineContext.ensureActive()
                sdkLogger.error(
                    "EventClient - consumeEvents: Exception during event consumption",
                    exception,
                    isRemoteLog = !request.isLogRequest
                )
                throw IOException(exception)
            } catch (throwable: Throwable) {
                onNetworkError?.invoke()
                sdkLogger.error(
                    "EventClient - consumeEvents: Throwable during event consumption (JavaScript)",
                    throwable,
                    isRemoteLog = !request.isLogRequest
                )
                throw IOException(throwable)
            }

        }
        val response = Response(
            request,
            httpResponse.status,
            httpResponse.headers,
            httpResponse.bodyAsText()
        )
        sdkLogger.debug(
            "log_request",
            buildJsonObject {
                put("url", request.url.toString())
                put("method", request.method.value)
                put("statusCode", response.status.value)
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
        sdkLogger.info(
            "log_request",
            buildJsonObject {
                put("url", request.url.toString())
                put("method", request.method.value)
                put("statusCode", response.status.value)
                put("networkingDuration", networkDuration.inWholeMilliseconds)
                put("retries", retries)
            },
            isRemoteLog = !request.isLogRequest
        )

        if (!httpResponse.status.isSuccess() && httpResponse.status != HttpStatusCode.Unauthorized) {
            if (retries == MAX_RETRY_COUNT) {
                sdkLogger.error(
                    LogEntry(
                        "Request retry limit reached!",
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
                throw RetryLimitReachedException("Request retry limit reached! Response: ${httpResponse.bodyAsText()}")
            }
            sdkLogger.error(
                LogEntry(
                    "Request failed with status code: ${httpResponse.status.value}",
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
            throw FailedRequestException(response)
        }

        return response
    }

    private fun shouldRetry(response: HttpResponse): Boolean {
        return !response.status.isSuccess() && (response.status.value == 408 || response.status.value == 429 || response.status.value !in 400..499)
    }
}
