package com.emarsys.core.networking

import com.emarsys.core.exceptions.SdkException
import com.emarsys.core.exceptions.SdkException.FailedRequestException
import com.emarsys.core.exceptions.SdkException.RetryLimitReachedException
import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.model.TestDataClass
import com.emarsys.testutil.MockHttpClientFactory
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.config
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestRetry

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.headersOf

import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.test.Test

class GenericNetworkClientTests {
    private lateinit var genericNetworkClient: GenericNetworkClient

    private val json: Json = JsonUtil.json

    private companion object {
        const val ID = "testId"
        const val NAME = "testName"
        val testData = TestDataClass(ID, NAME)
    }


    @Test
    fun testSend_should_send_request_that_is_successful() = runTest {
        createHttpClient(headers = headersOf(HttpHeaders.ContentType, "application/json"))
        val urlString = URLBuilder("https://denna.gservice.emarsys.net/echo").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Post,
            json.encodeToString(testData),
            headers = mapOf("Content-Type" to "application/json")
        )
        val expectedResponse = Result.success(
            Response(
                request,
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"),
                json.encodeToString(testData)
            )
        )

        val response: Result<Response> = genericNetworkClient.send(request)
        response shouldBe expectedResponse
    }

    @Test
    fun testSend_should_send_request_that_is_successful_without_headers() = runTest {
        createHttpClient()
        val urlString = URLBuilder("https://denna.gservice.emarsys.net/echo").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Post,
            json.encodeToString(testData),
            null
        )
        val expectedResponse = Result.success(
            Response(
                request,
                HttpStatusCode.OK,
                Headers.Empty,
                json.encodeToString(testData)
            )
        )

        val response: Result<Response> = genericNetworkClient.send(request)
        response shouldBe expectedResponse
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

        val response: Result<Response> = genericNetworkClient.send(request)
        response shouldBe Result.success(expectedResponse)
    }

    @Test
    fun testSend_should_send_request_that_fails_withoutRetries() = runTest {
        createHttpClient(HttpStatusCode.NotFound)
        val urlString =
            URLBuilder("https://denna.gservice.emarsys.net/customResponseCode/500").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Get,
            null,
            mapOf("Content-Type" to "application/json")
        )

        val response: Result<Response> = genericNetworkClient.send(request)

        response.exceptionOrNull() should beInstanceOf(FailedRequestException::class)
    }

    @Test
    fun testSend_returnWithFailureResultWithNetworkIOException_whenThereIsNoInternetConnection_andIOExceptionIsThrown() =
        runTest {
            val genericNetworkClient = GenericNetworkClient(
                HttpClient(MockEngine {
                    throw IOException("No internet connection")
                }),
                sdkLogger = mock(MockMode.autofill)
            )
            val urlString =
                URLBuilder("https://denna.gservice.emarsys.net/customResponseCode/500").build()
            val request = UrlRequest(
                urlString,
                HttpMethod.Get,
                null,
                mapOf("Content-Type" to "application/json")
            )
            val response: Result<Response> = genericNetworkClient.send(request)

            response.exceptionOrNull() should beInstanceOf(SdkException.NetworkIOException::class)
        }

    @Test
    fun testSend_returnWithFailureResultWithNetworkIOException_whenThereIsNoInternetConnection_andThrowableIsThrown() =
        runTest {
            val genericNetworkClient = GenericNetworkClient(
                HttpClient(MockEngine {
                    throw Throwable("No internet connection")
                }),
                sdkLogger = mock(MockMode.autofill)
            )
            val urlString =
                URLBuilder("https://denna.gservice.emarsys.net/customResponseCode/500").build()
            val request = UrlRequest(
                urlString,
                HttpMethod.Get,
                null,
                mapOf("Content-Type" to "application/json")
            )

            val response: Result<Response> = genericNetworkClient.send(request)

            response.exceptionOrNull() should beInstanceOf(SdkException.NetworkIOException::class)
        }

    @Test
    fun testSend_returnWithFailureResult_whenThereIsNoInternetConnection_andExceptionIsThrown() =
        runTest {
            val genericNetworkClient = GenericNetworkClient(
                HttpClient(MockEngine {
                    throw Exception("No internet connection")
                }),
                sdkLogger = mock(MockMode.autofill)
            )
            val urlString =
                URLBuilder("https://denna.gservice.emarsys.net/customResponseCode/500").build()
            val request = UrlRequest(
                urlString,
                HttpMethod.Get,
                null,
                mapOf("Content-Type" to "application/json")
            )
            val response: Result<Response> = genericNetworkClient.send(request)

            response.exceptionOrNull() should beInstanceOf(SdkException.NetworkIOException::class)
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
            install(HttpRequestRetry)
        }
        genericNetworkClient = GenericNetworkClient(httpClient, sdkLogger = mock(MockMode.autofill))
        val urlString =
            URLBuilder("https://denna.gservice.emarsys.net/customResponseCode/500").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Get,
            null,
            mapOf("Content-Type" to "application/json")
        )

        val response: Result<Response> = genericNetworkClient.send(request)
        val exception = response.exceptionOrNull() as RetryLimitReachedException

        exception should beInstanceOf(RetryLimitReachedException::class)
        exception.message shouldBe "Request retry limit reached!"
        exception.response.status shouldBe HttpStatusCode.InternalServerError
    }

    @Test
    fun testSend_should_send_request_that_succeeds_after_retries() = runTest {
        val mockHttpEngine = MockEngine.config {
            repeat(5) {
                addHandler {
                    respond(
                        ByteReadChannel(json.encodeToString(testData)),
                        status = HttpStatusCode.InternalServerError,
                        headers = Headers.Empty
                    )
                }
            }
            addHandler {
                respond(
                    ByteReadChannel(json.encodeToString(testData)),
                    status = HttpStatusCode.OK,
                    headers = Headers.Empty,
                )
            }
        }
        val httpClient = HttpClient(mockHttpEngine) {
            install(HttpRequestRetry)
        }
        genericNetworkClient = GenericNetworkClient(httpClient, sdkLogger = mock(MockMode.autofill))

        val urlString =
            URLBuilder("https://denna.gservice.emarsys.net/customResponseCode/500").build()
        val request = UrlRequest(
            urlString,
            HttpMethod.Get,
            null,
            mapOf("Content-Type" to "application/json")
        )
        val expectedResponse = Result.success(
            Response(
                request,
                HttpStatusCode.OK,
                Headers.Empty,
                json.encodeToString(testData)
            )
        )
        val response: Result<Response> = genericNetworkClient.send(request)
        response shouldBe expectedResponse
    }

    private fun createHttpClient(
        responseStatus: HttpStatusCode = HttpStatusCode.OK,
        headers: Headers = Headers.Empty,
        body: String = json.encodeToString(testData),
    ) {
        val httpClient = MockHttpClientFactory.create(
            responseStatus,
            headers,
            body
        )

        genericNetworkClient = GenericNetworkClient(httpClient, sdkLogger = mock(MockMode.autofill))
    }
}

