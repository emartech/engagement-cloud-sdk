package com.emarsys.core.networking

import com.emarsys.clients.event.model.CustomEvent
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
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
    fun testSend_should_send_request_that_fails_withoutRetries() = runTest {
        createHttpClient(HttpStatusCode.NotFound)
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

    @Test
    fun testSend_should_send_request_that_fails_withRetries() = runTest {
        val mockHttpEngine = MockEngine.config {
            repeat(6) {
                addHandler {
                    respond(
                        ByteReadChannel("""{"eventName":"test"}"""),
                        status = HttpStatusCode.InternalServerError,
                        headers = Headers.Empty
                    )
                }
            }
            addHandler {
                respond(
                    ByteReadChannel("""{"eventName":"test"}"""),
                    status = HttpStatusCode.OK,
                    headers = Headers.Empty,
                )
            }
        }
        val httpClient = HttpClient(mockHttpEngine) {
            install(ContentNegotiation) {
                json()
            }
            install(HttpRequestRetry)
        }
        genericNetworkClient = GenericNetworkClient(httpClient)
        val urlString = URLBuilder("https://denna.gservice.emarsys.net/customResponseCode/500").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Get,
            null,
            mapOf("Content-Type" to listOf("application/json"))
        )

        val exception = shouldThrow<RetryLimitReachedException> {
            genericNetworkClient.send(request)
        }
        exception.message shouldBe """Request retry limit reached! Response: {"eventName":"test"}"""
    }

    @Test
    fun testSend_should_send_request_that_succeeds_after_retries() = runTest {
        val mockHttpEngine = MockEngine.config {
            repeat(5) {
                addHandler {
                    respond(
                        ByteReadChannel("""{"eventName":"test"}"""),
                        status = HttpStatusCode.InternalServerError,
                        headers = Headers.Empty
                    )
                }
            }
            addHandler {
                respond(
                    ByteReadChannel("""{"eventName":"test"}"""),
                    status = HttpStatusCode.OK,
                    headers = Headers.Empty,
                )
            }
        }
        val httpClient = HttpClient(mockHttpEngine) {
            install(ContentNegotiation) {
                json()
            }
            install(HttpRequestRetry)
        }
        genericNetworkClient = GenericNetworkClient(httpClient)

        val urlString = URLBuilder("https://denna.gservice.emarsys.net/customResponseCode/500").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Get,
            null,
            mapOf("Content-Type" to listOf("application/json"))
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
            install(HttpRequestRetry)
        }
        genericNetworkClient = GenericNetworkClient(httpClient)
    }
}

