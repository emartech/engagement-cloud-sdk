package com.emarsys.networking.clients.device

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headers
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeviceClientTests {
    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi
    private lateinit var mockContactTokenHandler: ContactTokenHandlerApi
    private var sessionContext: SessionContext = SessionContext()
    private lateinit var deviceClient: DeviceClientApi

    @BeforeTest
    fun setUp() {
        mockEmarsysClient = mock()
        mockUrlFactory = mock()
        mockDeviceInfoCollector = mock()
        mockContactTokenHandler = mock()
        deviceClient = DeviceClient(mockEmarsysClient, mockUrlFactory, mockDeviceInfoCollector, mockContactTokenHandler)
    }

    @AfterTest
    fun teardown() {
        sessionContext = SessionContext()
    }

    @Test
    fun testRegisterDeviceInfo_should_send_deviceInfo_to_client_service() = runTest {
        val testUrl = Url("https://www.testUrl.com/testAppCode/client")
        val testDeviceInfoString = "testDeviceInfo"
        val testRefreshToken = "testRefreshToken"
        val testContactToken = "testContactToken"
        val request = UrlRequest(
            testUrl,
            HttpMethod.Post,
            testDeviceInfoString
        )
        val expectedResponse = Response(
            request,
            HttpStatusCode.OK,
            headers {
                append("Content-Type", "application/json")
                append("X-Client-State", "testClientState")
            },
            """{"refreshToken":"$testRefreshToken", "contactToken":"$testContactToken"}"""
        )
        every { mockDeviceInfoCollector.collect() } returns testDeviceInfoString
        every { mockUrlFactory.create(REGISTER_DEVICE_INFO) } returns testUrl
        everySuspend { mockEmarsysClient.send(any()) } returns expectedResponse
        every { mockContactTokenHandler.handleContactTokens(expectedResponse) } returns Unit

        deviceClient.registerDeviceInfo()

        verifySuspend {
            mockDeviceInfoCollector.collect()
            mockUrlFactory.create(REGISTER_DEVICE_INFO)
            mockEmarsysClient.send(request)
            mockContactTokenHandler.handleContactTokens(expectedResponse)
        }
    }
}