package com.emarsys.clients.push

import com.emarsys.EmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.DefaultUrlsApi
import com.emarsys.core.networking.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test


class PushClientTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockNetworkClient: NetworkClientApi

    @Mock
    lateinit var mockDefaultUrls: DefaultUrlsApi

    @Mock
    lateinit var mockSdkContext: SdkContextApi

    private var pushClient: PushClient by withMocks {
        PushClient(mockNetworkClient, mockSdkContext, mockDefaultUrls, Json.Default)
    }

    @Test
    fun testRegisterPushToken() = runTest {
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns EmarsysConfig("EMS11-C3FD3")
        every {
            mockSdkContext.createUrl(
                baseUrl = clientServiceBaseUrl,
                version = "v3",
                withAppCode = true,
                path = "/client/push-token"
            )
        } returns Url("$clientServiceBaseUrl/v3/apps/EMS11-C3FD3/client/push-token")

        val expectedUrlRequest = UrlRequest(
            Url("$clientServiceBaseUrl/v3/apps/EMS11-C3FD3/client/push-token"),
            HttpMethod.Put,
            """{"pushToken":"test"}"""
        )
        everySuspending { mockNetworkClient.send(expectedUrlRequest) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            ""
        )

        pushClient.registerPushToken("test")

        verifyWithSuspend(exhaustive = false) {
            mockNetworkClient.send(expectedUrlRequest)
        }
    }
}