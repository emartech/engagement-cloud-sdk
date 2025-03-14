package com.emarsys.networking.clients.push

import com.emarsys.EmarsysConfig
import com.emarsys.context.DefaultUrlsApi
import com.emarsys.context.SdkContextApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test


class PushClientTests {

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockDefaultUrls: DefaultUrlsApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var pushClient: PushClient

    @BeforeTest
    fun setUp() {
        mockEmarsysClient = mock()
        mockDefaultUrls = mock()
        mockSdkContext = mock()
        mockUrlFactory = mock()

        pushClient = PushClient(mockEmarsysClient, mockUrlFactory, Json)
    }

    @Test
    fun testRegisterPushToken() = runTest {
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns EmarsysConfig("EMS11-C3FD3")
        every {
            mockUrlFactory.create(EmarsysUrlType.PUSH_TOKEN, null)
        } returns Url("$clientServiceBaseUrl/v3/apps/EMS11-C3FD3/client/push-token")

        val expectedUrlRequest = UrlRequest(
            Url("$clientServiceBaseUrl/v3/apps/EMS11-C3FD3/client/push-token"),
            HttpMethod.Put,
            """{"pushToken":"test"}"""
        )
        everySuspend { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            ""
        )

        pushClient.registerPushToken("test")

        verifySuspend {
            mockEmarsysClient.send(expectedUrlRequest)
        }
    }

    @Test
    fun testClearPushToken() = runTest {
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns EmarsysConfig("EMS11-C3FD3")
        every {
            mockUrlFactory.create(EmarsysUrlType.PUSH_TOKEN, null)
        } returns Url("$clientServiceBaseUrl/v3/apps/EMS11-C3FD3/client/push-token")

        val expectedUrlRequest = UrlRequest(
            Url("$clientServiceBaseUrl/v3/apps/EMS11-C3FD3/client/push-token"),
            HttpMethod.Delete
        )
        everySuspend { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            ""
        )

        pushClient.clearPushToken()

        verifySuspend {
            mockEmarsysClient.send(expectedUrlRequest)
        }
    }
}