package com.emarsys.networking.clients.remoteConfig

import com.emarsys.core.crypto.CryptoApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.remoteConfig.RemoteConfigResponse
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class RemoteConfigClientTests {
    private lateinit var mockNetworkClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockCrypto: CryptoApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var remoteConfigClient: RemoteConfigClient

    companion object {
        const val configResult = """{"logLevel":"ERROR"}"""
        const val configSignatureResult = """<<<testSignature>>>"""
        val configUrl = Url("testRemoteConfigUrl")
        val configSignatureUrl = Url("testRemoteConfigSignatureUrl")
        val configRequest = UrlRequest(configUrl, HttpMethod.Get)
        val configSignatureRequest = UrlRequest(configSignatureUrl, HttpMethod.Get)
    }

    @BeforeTest
    fun setUp() {
        mockNetworkClient = mock()
        mockUrlFactory = mock()
        mockCrypto = mock()
        mockSdkLogger = mock()
        everySuspend { mockSdkLogger.error(any<String>(), any<Throwable>()) } returns Unit

        remoteConfigClient =
            RemoteConfigClient(mockNetworkClient, mockUrlFactory, mockCrypto, Json, mockSdkLogger)
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnRemoteConfig() = runTest {
        val configResponse = Response(configRequest, HttpStatusCode.OK, Headers.Empty, configResult)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            configSignatureResult
        )

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspend { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse
        everySuspend { mockCrypto.verify(any(), any()) } returns true

        val result = remoteConfigClient.fetchRemoteConfig()

        result shouldBe RemoteConfigResponse(logLevel = LogLevel.Error)
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnGlobalRemoteConfig() = runTest {
        val configResponse = Response(configRequest, HttpStatusCode.OK, Headers.Empty, configResult)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            configSignatureResult
        )

        every { mockUrlFactory.create(EmarsysUrlType.GLOBAL_REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.GLOBAL_REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspend { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse
        everySuspend { mockCrypto.verify(any(), any()) } returns true

        val result = remoteConfigClient.fetchRemoteConfig(global=true)

        result shouldBe RemoteConfigResponse(logLevel = LogLevel.Error)
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnNull() = runTest {
        val configResponse = Response(configRequest, HttpStatusCode.OK, Headers.Empty, configResult)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            configSignatureResult
        )

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspend { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse
        everySuspend { mockCrypto.verify(any(), any()) } returns false

        val result = remoteConfigClient.fetchRemoteConfig()

        result shouldBe null
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnNull_whenConfigIsNotFound() = runTest {
        val configResponse =
            Response(configRequest, HttpStatusCode.NotFound, Headers.Empty, configResult)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            configSignatureResult
        )

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspend { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse

        val result = remoteConfigClient.fetchRemoteConfig()

        verifySuspend(VerifyMode.exactly(0)) { mockCrypto.verify(any(), any()) }
        result shouldBe null
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnNull_whenSignatureIsNotFound() = runTest {
        val configResponse =
            Response(configRequest, HttpStatusCode.OK, Headers.Empty, configResult)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.NotFound,
            Headers.Empty,
            configSignatureResult
        )

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspend { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse

        val result = remoteConfigClient.fetchRemoteConfig()

        verifySuspend(VerifyMode.exactly(0)) { mockCrypto.verify(any(), any()) }
        result shouldBe null
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnNull_whenFetchingConfigThrowsException() = runTest {
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            configSignatureResult
        )

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } throws  Exception("test-exception")
        everySuspend { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse

        val result = remoteConfigClient.fetchRemoteConfig()

        verifySuspend(VerifyMode.exactly(0)) { mockCrypto.verify(any(), any()) }
        result shouldBe null
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnNull_whenFetchingSignatureThrowsException() = runTest {
        val configResponse =
            Response(configRequest, HttpStatusCode.OK, Headers.Empty, configResult)

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspend { mockNetworkClient.send(configSignatureRequest) } throws  Exception("test-exception")

        val result = remoteConfigClient.fetchRemoteConfig()

        verifySuspend(VerifyMode.exactly(0)) { mockCrypto.verify(any(), any()) }
        result shouldBe null
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnNull_whenSignatureIsNotVerified() = runTest {
        val configResponse = Response(configRequest, HttpStatusCode.OK, Headers.Empty, configResult)
        val configSignatureResponse = Response(
            configSignatureRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            configSignatureResult
        )

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspend { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse
        everySuspend { mockCrypto.verify(any(), any()) } returns false

        val result = remoteConfigClient.fetchRemoteConfig()

        result shouldBe null
    }

}