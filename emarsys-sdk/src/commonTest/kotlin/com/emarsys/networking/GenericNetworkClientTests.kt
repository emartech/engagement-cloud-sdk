package com.emarsys.networking

import com.emarsys.clients.event.model.CustomEvent
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.networking.model.Response
import com.emarsys.networking.model.UrlRequest
import com.emarsys.networking.model.body
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class GenericNetworkClientTests {
    private lateinit var genericNetworkClient: GenericNetworkClient


    @Test
    fun testSend_should_send_request_that_is_successful() = runTest {
        createHttpClient(headers = headersOf(HttpHeaders.ContentType, "application/json"))
        val urlString = URLBuilder("https://denna.gservice.emarsys.net/echo").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Post,
            """{"eventName":"test"}""",
            mapOf("Content-Type" to listOf("application/json"))
        )
        val expectedResponse = Response(
            request,
            HttpStatusCode.OK,
            headersOf(HttpHeaders.ContentType, "application/json"),
            """{"eventName":"test"}"""
        )

        val response: Response = genericNetworkClient.send(request)
        response shouldBe expectedResponse
        response.body<CustomEvent>() shouldBe CustomEvent("test")
    }

    @Test
    fun testSend_should_send_request_that_is_successful_without_headers() = runTest {
        createHttpClient()
        val urlString = URLBuilder("https://denna.gservice.emarsys.net/echo").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Post,
            """{"eventName":"test"}""",
            null
        )
        val expectedResponse = Response(
            request,
            HttpStatusCode.OK,
            Headers.Empty,
            """{"eventName":"test"}"""
        )

        val response: Response = genericNetworkClient.send(request)
        response shouldBe expectedResponse
        response.body<CustomEvent>() shouldBe CustomEvent("test")
    }

    @Test
    fun testSend_should_send_request_that_is_successful_without_body() = runTest {
        createHttpClient(body = "")
        val urlString = URLBuilder("https://denna.gservice.emarsys.net/echo").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Post,
            null,
            null
        )
        val expectedResponse = Response(
            request,
            HttpStatusCode.OK,
            Headers.Empty,
            ""
        )

        val response: Response = genericNetworkClient.send(request)
        response shouldBe expectedResponse
    }

    @Test
    fun testSend_should_send_request_that_fails() = runTest {
        createHttpClient(HttpStatusCode.InternalServerError)
        val urlString = URLBuilder("https://denna.gservice.emarsys.net/customResponseCode/500").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Get,
            null,
            mapOf("Content-Type" to listOf("application/json"))
        )

        shouldThrow<FailedRequestException> {
            genericNetworkClient.send(request)
        }
    }

    private fun createHttpClient(
        responseStatus: HttpStatusCode = HttpStatusCode.OK,
        headers: Headers = Headers.Empty,
        body: String = """{"eventName":"test"}"""
    ) {
        val mockHttpEngine = MockEngine {
            respond(
                ByteReadChannel(body),
                status = responseStatus,
                headers = headers
            )
        }
        val httpClient = HttpClient(mockHttpEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        genericNetworkClient = GenericNetworkClient(httpClient)
    }
}

