package com.emarsys.clients.device

import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.clients.device.DeviceClient
import com.emarsys.core.networking.clients.device.DeviceClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.session.SessionContext
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.url.FactoryApi
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headers
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.Test

class DeviceClientTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockEmarsysClient: NetworkClientApi

    @Mock
    lateinit var mockUrlFactory: FactoryApi<EmarsysUrlType, String>

    @Mock
    lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi

    private var sessionContext: SessionContext = SessionContext()

    private val deviceClient: DeviceClientApi by withMocks {
        DeviceClient(mockEmarsysClient, mockUrlFactory, mockDeviceInfoCollector, sessionContext)
    }

    @AfterTest
    fun teardown() {
        sessionContext = SessionContext()
        mocker.reset()
    }

    @Test
    fun registerDeviceInfo_should_send_deviceInfo_to_client_service() = runTest {
        val testUrl = "https://www.testUrl.com/testAppCode/client"
        val testDeviceInfoString = "testDeviceInfo"
        val testRefreshToken = "testRefreshToken"
        val testContactToken = "testContactToken"
        val request = UrlRequest(
            Url(testUrl),
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
        everySuspending { mockEmarsysClient.send(isAny()) } returns expectedResponse

        deviceClient.registerDeviceInfo()

        verifyWithSuspend {
            mockDeviceInfoCollector.collect()
            mockUrlFactory.create(REGISTER_DEVICE_INFO)
            mockEmarsysClient.send(request)
        }
        sessionContext.refreshToken shouldBe testRefreshToken
        sessionContext.contactToken shouldBe testContactToken
    }
}