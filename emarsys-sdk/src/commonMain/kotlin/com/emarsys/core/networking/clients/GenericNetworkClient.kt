package com.emarsys.core.networking.clients

import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.RetryLimitReachedException
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
import kotlin.time.Duration.Companion.seconds

class GenericNetworkClient(private val client: HttpClient) : NetworkClientApi {

    private companion object {
        const val MAX_RETRY_COUNT = 5
        val RETRY_DELAY = 2.seconds
    }

    override suspend fun send(request: UrlRequest): Response {
        var retries = 0
        val httpResponse = client.request {
            val shouldAddDefaultHeader = request.headers?.get(HttpHeaders.ContentType)?.toString().isNullOrEmpty()
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

        val response = Response(
            request,
            httpResponse.status,
            httpResponse.headers,
            httpResponse.bodyAsText()
        )
        if (!httpResponse.status.isSuccess() && httpResponse.status != HttpStatusCode.Unauthorized) {
            if (retries == MAX_RETRY_COUNT) {
                throw RetryLimitReachedException("Request retry limit reached! Response: ${httpResponse.bodyAsText()}")
            }
            throw FailedRequestException(response)
        }

        return response
    }

    private fun shouldRetry(response: HttpResponse): Boolean {
        return !response.status.isSuccess() && (response.status.value == 408 || response.status.value == 429 || response.status.value !in 400..499)
    }
}
