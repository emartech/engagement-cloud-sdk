package com.emarsys.testutil

import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel

class MockHttpClientFactory {
    companion object {
        fun create(
            responseStatus: HttpStatusCode = HttpStatusCode.OK,
            headers: Headers = Headers.Empty,
            body: String,
        ): HttpClient {
            val mockHttpEngine = MockEngine {
                it.headers[HttpHeaders.Accept] shouldBe "application/json"
                respond(
                    ByteReadChannel(body),
                    status = responseStatus,
                    headers = headers
                )
            }

            return HttpClient(mockHttpEngine) {
                install(ContentNegotiation) {
                    json()
                }
                install(HttpRequestRetry)
            }
        }
    }
}