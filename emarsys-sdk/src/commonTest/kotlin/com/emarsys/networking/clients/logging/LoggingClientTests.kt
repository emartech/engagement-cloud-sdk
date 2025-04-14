package com.emarsys.networking.clients.logging

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.device.DeviceInfoForLogs
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.event.model.OnlineSdkEvent
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.io.IOException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
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
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var mockEventsDao: EventsDaoApi

    @BeforeTest
    fun setup() = runTest {
        sdkDispatcher = StandardTestDispatcher()
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
        }
        mockSdkEventManager = mock()
        onlineEvents = MutableSharedFlow()
        everySuspend { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
        mockEventsDao = mock(MockMode.autofill)
    }

    private fun createLoggingClient(applicationScope: CoroutineScope): LoggingClient {
        return LoggingClient(
            mockEmarsysClient,
            mockUrlFactory,
            mockSdkEventManager,
            json,
            mockSdkLogger,
            applicationScope,
            mockDeviceInfoCollector,
            mockEventsDao
        )
    }

    @AfterTest
    fun tearDown() {
        resetCalls()
        resetAnswers()
    }

    @Test
    fun testConsumer_should_call_client_with_logEvent() = runTest {
        createLoggingClient(backgroundScope).register()

        every { mockUrlFactory.create(EmarsysUrlType.LOGGING, null) } returns TEST_BASE_URL
        everySuspend { mockEmarsysClient.send(any(), any()) } returns createTestResponse("{}")
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
            attributes = testLogAttributes
        )
        val expectedRequest = UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post,
            json.encodeToString(
                buildJsonObject {
                    put("logs", JsonArray(listOf(buildJsonObject {
                        put("type", "log_request")
                        put("level", logEvent.level.name.uppercase())
                        put(
                            "deviceInfo",
                            json.encodeToJsonElement(deviceInfoForLogs)
                        )
                        testLogAttributes.forEach { attribute ->
                            put(attribute.key, attribute.value)
                        }
                    })))
                }

            ),
            isLogRequest = true
        )

        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(logEvent)

        advanceTimeBy(11000)

        onlineSdkEvents.await() shouldBe listOf(logEvent)
        verifySuspend { mockEmarsysClient.send(expectedRequest, any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend { mockEventsDao.removeEvent(logEvent) }
    }

    @Test
    fun testConsumer_should_call_client_with_metricEvent() = runTest {
        createLoggingClient(backgroundScope).register()
        every { mockUrlFactory.create(EmarsysUrlType.LOGGING, null) } returns TEST_BASE_URL
        everySuspend { mockEmarsysClient.send(any(), any()) } returns createTestResponse("{}")
        everySuspend { mockDeviceInfoCollector.collectAsDeviceInfoForLogs() } returns deviceInfoForLogs
        val testLogAttributes = buildJsonObject {
            put("message", JsonPrimitive("Test metric message"))
            put("onScreenStart", JsonPrimitive("111"))
            put("onScreenEnd", JsonPrimitive("222"))
            put("onScreenDuration", JsonPrimitive("333"))
        }
        val logEvent = SdkEvent.Internal.Sdk.Metric(
            level = LogLevel.Metric,
            attributes = testLogAttributes
        )
        val expectedRequest = UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post,
            json.encodeToString(
                buildJsonObject {
                    put("logs", JsonArray(listOf(buildJsonObject {
                        put("type", "log_request")
                        put("level", logEvent.level.name.uppercase())
                        put(
                            "deviceInfo",
                            json.encodeToJsonElement(deviceInfoForLogs)
                        )
                        testLogAttributes.forEach { attribute ->
                            put(attribute.key, attribute.value)
                        }
                    })))
                }

            ),
            isLogRequest = true
        )
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(logEvent)
        advanceTimeBy(11000)

        onlineSdkEvents.await() shouldBe listOf(logEvent)
        verifySuspend { mockEmarsysClient.send(expectedRequest, any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend { mockEventsDao.removeEvent(logEvent) }
    }

    @Test
    fun testConsumer_should_reEmit_events_into_flow_when_there_is_a_network_error() = runTest {
        createLoggingClient(backgroundScope).register()
        every { mockUrlFactory.create(EmarsysUrlType.LOGGING, null) } returns TEST_BASE_URL
        everySuspend { mockEmarsysClient.send(any(), any()) } calls { args ->
            (args.arg(1) as suspend () -> Unit).invoke()
            throw IOException("No Internet")
        }
        everySuspend { mockDeviceInfoCollector.collectAsDeviceInfoForLogs() } returns deviceInfoForLogs
        val testLogAttributes = buildJsonObject {
            put("message", JsonPrimitive("Test metric message"))
            put("onScreenStart", JsonPrimitive("111"))
            put("onScreenEnd", JsonPrimitive("222"))
            put("onScreenDuration", JsonPrimitive("333"))
        }
        val logEvent = SdkEvent.Internal.Sdk.Metric(
            level = LogLevel.Metric,
            attributes = testLogAttributes
        )
        everySuspend { mockSdkEventManager.emitEvent(logEvent) } returns Unit
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(logEvent)
        advanceTimeBy(11000)

        onlineSdkEvents.await() shouldBe listOf(logEvent)
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verifySuspend { mockSdkLogger.error(any(), any<Throwable>(), false) }
        verifySuspend { mockSdkEventManager.emitEvent(logEvent) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(logEvent) }
    }

    @Test
    fun testConsumer_should_not_ack_event_and_log_locally_when_unknown_exception_happens() =
        runTest {
            createLoggingClient(backgroundScope).register()

            every { mockUrlFactory.create(EmarsysUrlType.LOGGING, null) } throws RuntimeException()
            everySuspend { mockDeviceInfoCollector.collectAsDeviceInfoForLogs() } returns deviceInfoForLogs
            val logEvent = SdkEvent.Internal.Sdk.Metric(
                level = LogLevel.Metric,
            )

            val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(logEvent)

            advanceTimeBy(11000)

            onlineSdkEvents.await() shouldBe listOf(logEvent)
            verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any(), any()) }
            verifySuspend { mockSdkLogger.error(any(), any<Throwable>(), false) }
            verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(logEvent) }
        }

    @Test
    fun testConsumer_should_ack_event_when_known_exception_happens() = forAll(
        table(
            headers("exception"),
            listOf(
                row(
                    FailedRequestException(
                        createTestResponse(
                            statusCode = HttpStatusCode.BadRequest
                        )
                    )
                ),
                row(RetryLimitReachedException("Retry limit reached")),
                row(
                    MissingApplicationCodeException("Missing app code")
                ),
            )
        )
    ) { testException ->
        runTest {
            createLoggingClient(backgroundScope).register()

            every { mockUrlFactory.create(EmarsysUrlType.LOGGING, null) } throws testException
            val logEvent = SdkEvent.Internal.Sdk.Metric(
                level = LogLevel.Metric
            )

            val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(logEvent)

            advanceTimeBy(11000)

            onlineSdkEvents.await() shouldBe listOf(logEvent)
            verifySuspend(VerifyMode.exactly(0)) { mockEmarsysClient.send(any(), any()) }
            verifySuspend(VerifyMode.exactly(0)) {
                mockSdkLogger.error(
                    any(),
                    any<Throwable>(),
                    false
                )
            }
            verifySuspend { mockEventsDao.removeEvent(logEvent) }
        }
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