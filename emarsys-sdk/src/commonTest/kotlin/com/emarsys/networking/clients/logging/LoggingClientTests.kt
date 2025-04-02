package com.emarsys.networking.clients.logging

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.DeviceInfoForLogs
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoggingClientTests {

    private companion object {
        val TEST_BASE_URL = Url("https://test-base-url/")
        val mockEmarsysClient: NetworkClientApi = mock()
        val mockUrlFactory: UrlFactoryApi = mock()
        val mockSdkLogger: Logger = mock(MockMode.autofill)
        val mockDeviceInfoCollector: DeviceInfoCollectorApi = mock()
        val json = JsonUtil.json
        val deviceInfoForLogs: DeviceInfoForLogs = DeviceInfoForLogs(
            "test",
            "mobile",
            null,
            null,
            "1.0.0",
            "testDevice",
            "8.0.0",
            "4.0.0",
            true,
            null,
            null,
            "hu_HU",
            "+0200",
            "testClientId"
        )
    }

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var sdkEventDistributor: SdkEventDistributorApi
    private lateinit var onlineEvents: MutableSharedFlow<SdkEvent>

    @BeforeTest
    fun setup() = runTest {
        sdkDispatcher = StandardTestDispatcher()
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
            throw it.args[1] as Throwable
        }
        sdkEventDistributor = mock()
        onlineEvents = MutableSharedFlow()
        everySuspend { sdkEventDistributor.onlineEvents } returns onlineEvents

        LoggingClient(
            mockEmarsysClient,
            mockUrlFactory,
            sdkEventDistributor,
            json,
            mockSdkLogger,
            sdkDispatcher,
            mockDeviceInfoCollector
        )
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    @Test
    fun testConsumer_should_call_client_with_logEvent() = runTest {
        every { mockUrlFactory.create(EmarsysUrlType.LOGGING, null) } returns TEST_BASE_URL
        everySuspend { mockEmarsysClient.send(any()) } returns createTestResponse("{}")
        everySuspend { mockDeviceInfoCollector.collectAsDeviceInfoForLogs() } returns deviceInfoForLogs
        val testLogAttributes = buildJsonObject {
            put("message", JsonPrimitive("Test log message"))
            put("url", JsonPrimitive("https://test.url"))
            put("statusCode", JsonPrimitive("200"))
            put("networkingDuration", JsonPrimitive("123"))
            put("networkingEnd", JsonPrimitive("456"))
            put("networkingStart", JsonPrimitive("123"))
        }
        val logEvent = SdkEvent.Internal.Sdk.Log(
            level = LogLevel.Debug,
            name = "log",
            attributes = testLogAttributes
        )
        CoroutineScope(sdkDispatcher).launch {
            onlineEvents.emit(logEvent)
        }

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    @Test
    fun testConsumer_should_call_client_with_metricEvent() = runTest {
        every { mockUrlFactory.create(EmarsysUrlType.LOGGING, null) } returns TEST_BASE_URL
        everySuspend { mockEmarsysClient.send(any()) } returns createTestResponse("{}")
        everySuspend { mockDeviceInfoCollector.collectAsDeviceInfoForLogs() } returns deviceInfoForLogs
        val testLogAttributes = buildJsonObject {
            put("message", JsonPrimitive("Test metric message"))
            put("onScreenStart", JsonPrimitive("111"))
            put("onScreenEnd", JsonPrimitive("222"))
            put("onScreenDuration", JsonPrimitive("333"))
        }
        val logEvent = SdkEvent.Internal.Sdk.Metric(
            level = LogLevel.Metric,
            name = "metric",
            attributes = testLogAttributes
        )

        onlineEvents.emit(logEvent)
        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    private fun createTestResponse(
        body: String = "{}",
        statusCode: HttpStatusCode = HttpStatusCode.OK
    ) = Response(
        UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post
        ), statusCode, Headers.Empty, bodyAsText = body
    )
}