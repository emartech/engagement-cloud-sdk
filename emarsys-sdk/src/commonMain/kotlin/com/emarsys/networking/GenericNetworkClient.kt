package com.emarsys.networking

import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.networking.model.Response
import com.emarsys.networking.model.UrlRequest
import com.emarsys.networking.model.isRetryable
import com.emarsys.networking.model.isSuccess
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.time.Duration.Companion.seconds

class GenericNetworkClient(private val client: HttpClient) : NetworkClient, Retrier() {

    private companion object {
        const val RETRY_COUNT = 5
        val RETRY_DELAY = 2.seconds
    }

    override suspend fun send(request: UrlRequest): Response {
        val response = retry(RETRY_COUNT, RETRY_DELAY, ::shouldRetry) {
            val httpResponse = client.request {
                method = request.method
                url(request.urlString)
                request.headers?.forEach {
                    header(it.key, it.value)
                }
                request.bodyString?.let { setBody(request.bodyString) }
            }
            val bodyAsText = httpResponse.bodyAsText()

            Response(
                request,
                httpResponse.status,
                httpResponse.headers,
                bodyAsText
            )
        }

        if (response.isSuccess()) {
            return response
        } else {
            throw FailedRequestException(response)
        }
    }

    private fun shouldRetry(response: Response): Boolean {
        return response.isRetryable()
    }
}
