package com.emarsys.networking.clients.push

import com.emarsys.EmarsysConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.context.DefaultUrlsApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.UrlFactoryApi
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test


class PushClientTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockEmarsysClient: NetworkClientApi

    @Mock
    lateinit var mockDefaultUrls: DefaultUrlsApi

    @Mock
    lateinit var mockSdkContext: SdkContextApi

    @Mock
    lateinit var mockUrlFactory: UrlFactoryApi

    private var pushClient: PushClient by withMocks {
        PushClient(mockEmarsysClient, mockUrlFactory, Json)
    }

    @Test
    fun testRegisterPushToken() = runTest {
        val clientServiceBaseUrl = "https://me-client.eservice.emarsys.net"
        every { mockDefaultUrls.clientServiceBaseUrl } returns clientServiceBaseUrl
        every { mockSdkContext.config } returns EmarsysConfig("EMS11-C3FD3")
        every {
            mockUrlFactory.create(EmarsysUrlType.REGISTER_PUSH_TOKEN)
        } returns Url("$clientServiceBaseUrl/v3/apps/EMS11-C3FD3/client/push-token")

        val expectedUrlRequest = UrlRequest(
            Url("$clientServiceBaseUrl/v3/apps/EMS11-C3FD3/client/push-token"),
            HttpMethod.Put,
            """{"pushToken":"test"}"""
        )
        everySuspending { mockEmarsysClient.send(expectedUrlRequest) } returns Response(
            expectedUrlRequest,
            HttpStatusCode.OK,
            Headers.Empty,
            ""
        )

        pushClient.registerPushToken("test")

        verifyWithSuspend(exhaustive = false) {
            mockEmarsysClient.send(expectedUrlRequest)
        }
    }
}