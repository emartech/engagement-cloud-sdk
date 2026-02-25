package com.sap.ec.networking.clients.jsbridge

import com.sap.ec.context.DefaultUrlsApi
import com.sap.ec.context.Features
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.crypto.CryptoApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.StringStorageApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class JsBridgeClientTests {
    private companion object {
        const val JS_BRIDGE_URL = "https://example.com/jsbridge/latest.js"
        const val JS_BRIDGE_SIGNATURE_URL = "https://example.com/jsbridge/latest.sign"
        const val JS_CONTENT = "function jsBridge() { /* bridge code */ }"
        const val SIGNATURE = "testSignatureBase64"
        const val SERVER_MD5 = "rL0Y20zC+Fzt72VPzMSk2A=="
    }

    private lateinit var mockNetworkClient: NetworkClientApi
    private lateinit var mockCrypto: CryptoApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var jsBridgeClient: JsBridgeClient

    @BeforeTest
    fun setup() {
        mockNetworkClient = mock(MockMode.autofill)
        mockCrypto = mock(MockMode.autofill)
        mockSdkContext = mock(MockMode.autofill)
        mockStringStorage = mock(MockMode.autofill)
        mockSdkLogger = mock(MockMode.autofill)
        mockDefaultUrls = mock(MockMode.autofill)

        every { mockSdkContext.defaultUrls } returns mockDefaultUrls
        every { mockDefaultUrls.jsBridgeUrl } returns JS_BRIDGE_URL
        every { mockDefaultUrls.jsBridgeSignatureUrl } returns JS_BRIDGE_SIGNATURE_URL
        every { mockSdkContext.features } returns mutableSetOf(Features.JsBridgeSignatureCheck)

        jsBridgeClient = JsBridgeClient(
            networkClient = mockNetworkClient,
            crypto = mockCrypto,
            sdkContext = mockSdkContext,
            stringStorage = mockStringStorage,
            sdkLogger = mockSdkLogger
        )
    }

    private fun createResponse(
        body: String,
        url: String = JS_BRIDGE_URL,
        googHashHeader: String? = null
    ): Response {
        val headers = if (googHashHeader != null) {
            headersOf("x-goog-hash" to listOf(googHashHeader))
        } else {
            headersOf()
        }
        return Response(
            originalRequest = UrlRequest(
                url = Url(url),
                method = HttpMethod.Get
            ),
            status = HttpStatusCode.OK,
            headers = headers,
            bodyAsText = body
        )
    }

    // --- Feature ON tests ---

    @Test
    fun testValidateJSBridge_shouldReturnSuccess_andCacheMd5_whenVerificationSucceeds() = runTest {
        everySuspend { mockNetworkClient.send(any()) } sequentially {
            returns(Result.success(createResponse(JS_CONTENT, googHashHeader = "crc32c=abc, md5=$SERVER_MD5")))
            returns(Result.success(createResponse(SIGNATURE)))
        }
        everySuspend { mockCrypto.verify(JS_CONTENT, SIGNATURE) } returns true
        every { mockStringStorage.put(any(), any<String>()) } returns Unit

        val result = jsBridgeClient.validateJSBridge()

        result.isSuccess shouldBe true
        verify { mockStringStorage.put(StorageConstants.JS_BRIDGE_MD5_KEY, SERVER_MD5) }
    }

    @Test
    fun testValidateJSBridge_shouldReturnSuccess_andCacheNull_whenHeaderMissing() = runTest {
        everySuspend { mockNetworkClient.send(any()) } sequentially {
            returns(Result.success(createResponse(JS_CONTENT)))
            returns(Result.success(createResponse(SIGNATURE)))
        }
        everySuspend { mockCrypto.verify(JS_CONTENT, SIGNATURE) } returns true
        every { mockStringStorage.put(any(), any<String?>()) } returns Unit

        val result = jsBridgeClient.validateJSBridge()

        result.isSuccess shouldBe true
        verify { mockStringStorage.put(StorageConstants.JS_BRIDGE_MD5_KEY, null) }
    }

    @Test
    fun testValidateJSBridge_shouldReturnFailure_andClearHash_whenVerificationFails() = runTest {
        everySuspend { mockNetworkClient.send(any()) } sequentially {
            returns(Result.success(createResponse(JS_CONTENT, googHashHeader = "md5=$SERVER_MD5")))
            returns(Result.success(createResponse(SIGNATURE)))
        }
        everySuspend { mockCrypto.verify(JS_CONTENT, SIGNATURE) } returns false
        every { mockStringStorage.put(any(), any<String?>()) } returns Unit

        val result = jsBridgeClient.validateJSBridge()

        result.isFailure shouldBe true
        verify { mockStringStorage.put(StorageConstants.JS_BRIDGE_MD5_KEY, null) }
    }

    @Test
    fun testValidateJSBridge_shouldReturnFailure_whenJsFetchFails() = runTest {
        everySuspend { mockNetworkClient.send(any()) } returns
            Result.failure(Exception("network error"))

        val result = jsBridgeClient.validateJSBridge()

        result.isFailure shouldBe true
    }

    @Test
    fun testValidateJSBridge_shouldReturnFailure_whenSignatureFetchFails() = runTest {
        everySuspend { mockNetworkClient.send(any()) } sequentially {
            returns(Result.success(createResponse(JS_CONTENT, googHashHeader = "md5=$SERVER_MD5")))
            returns(Result.failure(Exception("signature download error")))
        }

        val result = jsBridgeClient.validateJSBridge()

        result.isFailure shouldBe true
    }

    @Test
    fun testValidateJSBridge_shouldParseMd5Only_fromGoogHashHeader() = runTest {
        everySuspend { mockNetworkClient.send(any()) } sequentially {
            returns(Result.success(createResponse(JS_CONTENT, googHashHeader = "md5=$SERVER_MD5")))
            returns(Result.success(createResponse(SIGNATURE)))
        }
        everySuspend { mockCrypto.verify(JS_CONTENT, SIGNATURE) } returns true
        every { mockStringStorage.put(any(), any<String>()) } returns Unit

        jsBridgeClient.validateJSBridge()

        verify { mockStringStorage.put(StorageConstants.JS_BRIDGE_MD5_KEY, SERVER_MD5) }
    }

    // --- Feature OFF tests ---

    @Test
    fun testValidateJSBridge_shouldFetchJsOnly_whenFeatureIsOff() = runTest {
        every { mockSdkContext.features } returns mutableSetOf()
        everySuspend { mockNetworkClient.send(any()) } returns
            Result.success(createResponse(JS_CONTENT, googHashHeader = "md5=$SERVER_MD5"))
        every { mockStringStorage.put(any(), any<String>()) } returns Unit

        val result = jsBridgeClient.validateJSBridge()

        result.isSuccess shouldBe true
        verifySuspend(dev.mokkery.verify.VerifyMode.exactly(1)) {
            mockNetworkClient.send(any())
        }
    }

    @Test
    fun testValidateJSBridge_shouldNotVerifySignature_whenFeatureIsOff() = runTest {
        every { mockSdkContext.features } returns mutableSetOf()
        everySuspend { mockNetworkClient.send(any()) } returns
            Result.success(createResponse(JS_CONTENT, googHashHeader = "md5=$SERVER_MD5"))
        every { mockStringStorage.put(any(), any<String>()) } returns Unit

        jsBridgeClient.validateJSBridge()

        verifySuspend(dev.mokkery.verify.VerifyMode.exactly(0)) {
            mockCrypto.verify(any(), any())
        }
    }

    @Test
    fun testValidateJSBridge_shouldCacheMd5_whenFeatureIsOff() = runTest {
        every { mockSdkContext.features } returns mutableSetOf()
        everySuspend { mockNetworkClient.send(any()) } returns
            Result.success(createResponse(JS_CONTENT, googHashHeader = "md5=$SERVER_MD5"))
        every { mockStringStorage.put(any(), any<String>()) } returns Unit

        jsBridgeClient.validateJSBridge()

        verify { mockStringStorage.put(StorageConstants.JS_BRIDGE_MD5_KEY, SERVER_MD5) }
    }

    @Test
    fun testValidateJSBridge_shouldReturnFailure_whenFeatureIsOff_andJsFetchFails() = runTest {
        every { mockSdkContext.features } returns mutableSetOf()
        everySuspend { mockNetworkClient.send(any()) } returns
            Result.failure(Exception("network error"))

        val result = jsBridgeClient.validateJSBridge()

        result.isFailure shouldBe true
    }

    // --- fetchServerMd5 tests ---

    @Test
    fun fetchServerMd5_shouldReturnMd5_whenHeaderPresent() = runTest {
        val headResponse = Response(
            originalRequest = UrlRequest(url = Url(JS_BRIDGE_URL), method = HttpMethod.Head),
            status = HttpStatusCode.OK,
            headers = headersOf("x-goog-hash" to listOf("crc32c=abc, md5=$SERVER_MD5")),
            bodyAsText = ""
        )
        everySuspend { mockNetworkClient.send(any()) } returns Result.success(headResponse)

        val result = jsBridgeClient.fetchServerMd5()

        result shouldBe Result.success(SERVER_MD5)
    }

    @Test
    fun fetchServerMd5_shouldReturnFailure_whenHeaderMissing() = runTest {
        val headResponse = Response(
            originalRequest = UrlRequest(url = Url(JS_BRIDGE_URL), method = HttpMethod.Head),
            status = HttpStatusCode.OK,
            headers = headersOf(),
            bodyAsText = ""
        )
        everySuspend { mockNetworkClient.send(any()) } returns Result.success(headResponse)

        val result = jsBridgeClient.fetchServerMd5()

        result.isFailure shouldBe true
    }

    @Test
    fun fetchServerMd5_shouldReturnFailure_whenNetworkFails() = runTest {
        everySuspend { mockNetworkClient.send(any()) } returns
            Result.failure(Exception("network error"))

        val result = jsBridgeClient.fetchServerMd5()

        result.isFailure shouldBe true
    }
}
