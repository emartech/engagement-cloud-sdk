package com.emarsys.networking

import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.networking.model.Response
import com.emarsys.networking.model.UrlRequest
import com.emarsys.networking.model.isSuccess
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class GenericNetworkClient(val client: HttpClient) : NetworkClient {
    override suspend fun send(request: UrlRequest): Response {
        val response = client.request {
            method = request.method
            url(request.urlString)
            request.headers?.forEach {
                header(it.key, it.value)
            }
            request.bodyString?.let { setBody(request.bodyString) }
        }

        val bodyAsText = response.bodyAsText()

        val result = Response(
            request,
            response.status,
            response.headers,
            bodyAsText
        )
        if (result.isSuccess()) {
            return result
        } else {
            throw FailedRequestException(result)
        }
    }
}

