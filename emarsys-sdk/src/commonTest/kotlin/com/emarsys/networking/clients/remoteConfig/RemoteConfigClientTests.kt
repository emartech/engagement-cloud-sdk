package com.emarsys.networking.clients.remoteConfig

import com.emarsys.core.crypto.CryptoApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.remoteConfig.RemoteConfigResponse
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
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
    private lateinit var remoteConfigClient: RemoteConfigClient

    @BeforeTest
    fun setUp() {
        mockNetworkClient = mock()
        mockUrlFactory = mock()
        mockCrypto = mock()

        remoteConfigClient = RemoteConfigClient(mockNetworkClient, mockUrlFactory, mockCrypto, Json)
    }

    @Test
    fun testFetchRemoteConfig_shouldReturnRemoteConfig() = runTest {
        val configResult = """{"logLevel":"ERROR"}"""
        val configSignatureResult = """<<<testSignature>>>"""
        val configUrl = Url("testRemoteConfigUrl")
        val configSignatureUrl = Url("testRemoteConfigSignatureUrl")
        val configRequest = UrlRequest(configUrl, HttpMethod.Get)
        val configResponse = Response(configRequest, HttpStatusCode.OK, Headers.Empty, configResult)
        val configSignatureRequest = UrlRequest(configSignatureUrl, HttpMethod.Get)
        val configSignatureResponse = Response(configSignatureRequest, HttpStatusCode.OK, Headers.Empty, configSignatureResult)

        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG) } returns configUrl
        every { mockUrlFactory.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE) } returns configSignatureUrl
        everySuspend { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspend { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse
        everySuspend { mockCrypto.verify(any(), any()) } returns true

        val result = remoteConfigClient.fetchRemoteConfig()

        result shouldBe RemoteConfigResponse(logLevel = LogLevel.Error)
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
        everySuspend { mockNetworkClient.send(configRequest) } returns configResponse
        everySuspend { mockNetworkClient.send(configSignatureRequest) } returns configSignatureResponse
        everySuspend { mockCrypto.verify(any(), any()) } returns false

        val result = remoteConfigClient.fetchRemoteConfig()

        result shouldBe null
    }

}