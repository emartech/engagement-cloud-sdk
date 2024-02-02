package com.emarsys.networking.clients.remoteConfig

import com.emarsys.core.crypto.Crypto
import com.emarsys.core.crypto.CryptoApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.remoteConfig.RemoteConfigResponse
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class RemoteConfigClientTests: TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockNetworkClient: NetworkClientApi

    @Mock
    lateinit var mockUrlFactory: UrlFactoryApi

    @Mock
    lateinit var mockCrypto: CryptoApi

    private var remoteConfigClient: RemoteConfigClient by withMocks {
        RemoteConfigClient(mockNetworkClient, mockUrlFactory, mockCrypto, Json)
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnRemoteConfig() = runTest {
        val configResult = """{"logLevel":"error"}"""
        val configSignatureResult = """<<<testSignature>>>"""
        val configUrl = Url("testRemoteConfigUrl")
        val configSignatureUrl = Url("testRemoteConfigSignatureUrl")
        val configRequest = UrlRequest(configUrl, HttpMethod.Get)
        val configResponse = Response(configRequest, HttpStatusCode.OK, Headers.Empty, configResult)
        val configSignatureRequest = UrlRequest(configSignatureUrl, HttpMethod.Get)
        val configSignatureResponse = Response(configSignatureRequest, HttpStatusCode.OK, Headers.Empty, configSignatureResult)

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspending { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspending { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse
        everySuspending { mockCrypto.verify(isAny(), isAny()) } returns true

        val result = remoteConfigClient.fetchRemoteConfig()

        result shouldBe RemoteConfigResponse(logLevel = LogLevel.error)
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnNull() = runTest {
        val configResult = """{"logLevel":"error"}"""
        val configSignatureResult = """<<<testSignature>>>"""
        val configUrl = Url("testRemoteConfigUrl")
        val configSignatureUrl = Url("testRemoteConfigSignatureUrl")
        val configRequest = UrlRequest(configUrl, HttpMethod.Get)
        val configResponse = Response(configRequest, HttpStatusCode.OK, Headers.Empty, configResult)
        val configSignatureRequest = UrlRequest(configSignatureUrl, HttpMethod.Get)
        val configSignatureResponse = Response(configSignatureRequest, HttpStatusCode.OK, Headers.Empty, configSignatureResult)

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspending { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspending { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse
        everySuspending { mockCrypto.verify(isAny(), isAny()) } returns false

        val result = remoteConfigClient.fetchRemoteConfig()

        result shouldBe null
    }

}