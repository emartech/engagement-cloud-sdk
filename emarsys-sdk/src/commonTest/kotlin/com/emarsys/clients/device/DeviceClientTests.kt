package com.emarsys.clients.device

import com.emarsys.core.device.DeviceInfo
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.clients.device.DeviceClient
import com.emarsys.core.networking.clients.device.DeviceClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.networking.EmarsysHeaders.CLIENT_ID_HEADER
import com.emarsys.networking.EmarsysHeaders.REQUEST_ORDER_HEADER
import com.emarsys.providers.Provider
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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.Test

class DeviceClientTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockNetworkClient: NetworkClientApi

    @Mock
    lateinit var mockUrlFactory: FactoryApi<EmarsysUrlType, String>

    @Mock
    lateinit var mockDeviceInfoCollector: DeviceInfoCollectorApi

    @Mock
    lateinit var mockTimestampProvider: Provider<Instant>

    private var sessionContext: SessionContext = SessionContext()

    private val now = Clock.System.now()

    private val json = Json

    private val deviceClient: DeviceClientApi by withMocks {
        DeviceClient(mockNetworkClient, mockUrlFactory, mockDeviceInfoCollector, sessionContext, mockTimestampProvider, json)
    }

    @AfterTest
    fun teardown() {
        sessionContext = SessionContext()
        mocker.reset()
    }

    @Test
    fun registerDeviceInfo_should_send_deviceInfo_to_client_service() = runTest {
        val testUrl = "https://www.testUrl.com/testAppCode/client"
        val testDeviceInfoString = json.encodeToString(createDeviceInfo())
        val testRefreshToken = "testRefreshToken"
        val testContactToken = "testContactToken"
        val request = UrlRequest(
            Url(testUrl),
            HttpMethod.Post,
            testDeviceInfoString,
            mapOf(
                CLIENT_ID_HEADER to "testHardwareId",
                REQUEST_ORDER_HEADER to now.toString()
            )
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
        every { mockTimestampProvider.provide() } returns now
        everySuspending { mockNetworkClient.send(isAny()) } returns expectedResponse

        deviceClient.registerDeviceInfo()

        verifyWithSuspend {
            mockDeviceInfoCollector.collect()
            mockUrlFactory.create(REGISTER_DEVICE_INFO)
            mockTimestampProvider.provide()
            mockNetworkClient.send(request)
        }
        sessionContext.refreshToken shouldBe testRefreshToken
        sessionContext.contactToken shouldBe testContactToken
    }

    private fun createDeviceInfo() = DeviceInfo(
        "platform",
        "applicationVersion",
        "deviceModel",
        "manufacturer",
        "displayMetrics",
        "osVersion",
        "sdkVersion",
        "language",
        "timezone",
        null,
        null,
        false,
        "testHardwareId",
    )
}