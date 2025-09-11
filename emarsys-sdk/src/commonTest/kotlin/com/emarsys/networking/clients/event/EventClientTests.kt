package com.emarsys.networking.clients.event

import com.emarsys.api.inapp.InAppConfigApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicAppEventActionModel
import com.emarsys.mobileengage.action.models.BasicOpenExternalUrlActionModel
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppType
import com.emarsys.mobileengage.inapp.InAppViewApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.WebViewHolder
import com.emarsys.networking.clients.error.ClientExceptionHandler
import com.emarsys.networking.clients.event.model.ContentCampaign
import com.emarsys.networking.clients.event.model.DeviceEventResponse
import com.emarsys.networking.clients.event.model.OnEventActionCampaign
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okio.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class EventClientTests {
    private companion object {
        const val DEVICE_EVENT_STATE = "testDeviceEventState"
        const val EVENT_NAME = "test event name"
        const val UUID = "testUuid"
        const val IN_APP_DND = false
        val TIMESTAMP = Clock.System.now()
        const val TRACKING_INFO = "testTrackingInfo"
        const val TRACKING_INFO_1 = "testTrackingInfo1"
        val TEST_BASE_URL = Url("https://test-base-url/")
        val testEventAttributes = buildJsonObject {
            put("key", JsonPrimitive("value"))
            put("key1", JsonPrimitive(5))

        }
        val testEvent = SdkEvent.External.Custom(
            id = UUID,
            name = EVENT_NAME,
            attributes = testEventAttributes,
            timestamp = TIMESTAMP
        )
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockClientExceptionHandler: ClientExceptionHandler
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockOnEventActionFactory: EventActionFactoryApi
    private lateinit var mockSdkEventManager: SdkEventManagerApi
    private lateinit var onlineEvents: MutableSharedFlow<OnlineSdkEvent>
    private lateinit var mockEventsDao: EventsDaoApi
    private lateinit var mockInAppConfigApi: InAppConfigApi
    private lateinit var mockInAppPresenter: InAppPresenterApi
    private lateinit var mockInAppViewProvider: InAppViewProviderApi
    private lateinit var mockInAppView: InAppViewApi
    private lateinit var mockRequestContext: RequestContextApi
    private lateinit var json: Json
    private lateinit var eventClient: EventClient
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockWebViewHolder: WebViewHolder
    private lateinit var mockUuidProvider: UuidProviderApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEmarsysClient = mock()
        mockClientExceptionHandler = mock(MockMode.autofill)
        mockUrlFactory = mock()
        mockOnEventActionFactory = mock()
        mockInAppConfigApi = mock()
        mockInAppPresenter = mock()
        mockInAppViewProvider = mock()
        mockInAppView = mock()
        mockSdkLogger = mock(MockMode.autofill)
        json = JsonUtil.json
        mockWebViewHolder = mock()
        mockRequestContext = mock(MockMode.autofill)
        every { mockRequestContext.deviceEventState } returns null
        mockEventsDao = mock(MockMode.autofill)
        onlineEvents = MutableSharedFlow()
        mockSdkEventManager = mock()
        mockUuidProvider = mock()
        every { mockUuidProvider.provide() } returns UUID
        every { mockSdkEventManager.onlineSdkEvents } returns onlineEvents
        everySuspend { mockSdkEventManager.emitEvent(any()) } returns Unit
        everySuspend { mockSdkEventManager.registerEvent(any()) } returns mock()
        every { mockInAppConfigApi.inAppDnd }.returns(IN_APP_DND)
        every { mockUrlFactory.create(EmarsysUrlType.EVENT) }.returns(TEST_BASE_URL)
        everySuspend { mockSdkLogger.error(any(), any<Throwable>(), true) } returns Unit
        everySuspend { mockInAppViewProvider.provide() } returns mockInAppView
        everySuspend { mockInAppView.load(any()) } returns mockWebViewHolder
    }

    private fun createEventClient(applicationScope: CoroutineScope) = EventClient(
        mockEmarsysClient,
        mockClientExceptionHandler,
        mockUrlFactory,
        json,
        mockOnEventActionFactory,
        mockRequestContext,
        mockInAppConfigApi,
        mockInAppPresenter,
        mockInAppViewProvider,
        mockSdkEventManager,
        mockEventsDao,
        mockSdkLogger,
        applicationScope,
        mockUuidProvider
    )

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRegister_should_start_consume_on_channel() = runTest {
        eventClient = createEventClient(backgroundScope)
        eventClient.register()

        verifySuspend { mockSdkEventManager.onlineSdkEvents.filter(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    @Test
    fun testConsumer_should_call_client_with_correct_request() = runTest {
        createEventClient(backgroundScope).register()

        every { mockRequestContext.deviceEventState } returns DEVICE_EVENT_STATE

        everySuspend { mockEmarsysClient.send(any()) } returns createTestResponse()
        every { mockUrlFactory.create(EmarsysUrlType.EVENT) } returns TEST_BASE_URL
        val expectedUrlRequest = UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post,
            """{"dnd":$IN_APP_DND,"events":[{"type":"custom","name":"${testEvent.name}","timestamp":"$TIMESTAMP","attributes":{"key":"value","key1":"5"}}],"deviceEventState":"$DEVICE_EVENT_STATE"}""",
        )
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(testEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent)
        verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    @Test
    fun testConsumer_shouldHandleDeviceEventState() = runTest {
        eventClient = createEventClient(backgroundScope)
        eventClient.register()

        val expectedDeviceEventState = "responseDeviceEventState"

        val deviceEventResponse = DeviceEventResponse(null, null, expectedDeviceEventState)
        val responseBody = json.encodeToString(deviceEventResponse)

        everySuspend { mockEmarsysClient.send(any()) }.returns(
            createTestResponse(
                responseBody
            )
        )
        every { mockUrlFactory.create(EmarsysUrlType.EVENT) }.returns(TEST_BASE_URL)
        val expectedUrlRequest = createTestRequest()
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(testEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent)
        verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

        verify { mockRequestContext.deviceEventState = expectedDeviceEventState }
    }

    @Test
    fun testConsumer_shouldHandleInApp_whenHtml_isPresent() = runTest {
        eventClient = createEventClient(backgroundScope)
        eventClient.register()

        val html = "testHtml"
        val testInapp = ContentCampaign(InAppType.OVERLAY, TRACKING_INFO, html)
        val expectedInAppMessage =
            InAppMessage(
                dismissId = UUID,
                InAppType.OVERLAY,
                trackingInfo = TRACKING_INFO,
                content = html
            )
        val deviceEventResponse = DeviceEventResponse(listOf(testInapp), null, DEVICE_EVENT_STATE)
        val body = json.encodeToString(deviceEventResponse)

        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse(body))
        everySuspend { mockInAppPresenter.present(any(), any(), any()) }.returns(Unit)
        val expectedUrlRequest = createTestRequest()
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(testEvent)
        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent)
        verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
        verifySuspend { mockInAppViewProvider.provide() }
        verifySuspend { mockInAppView.load(expectedInAppMessage) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    @Test
    fun testConsumer_shouldNotHandleInApp_whenHtml_IsEmpty() = runTest {
        eventClient = createEventClient(backgroundScope)
        eventClient.register()

        val html = ""
        val testInapp = ContentCampaign(InAppType.OVERLAY, TRACKING_INFO, html)
        val deviceEventResponse = DeviceEventResponse(listOf(testInapp), null, DEVICE_EVENT_STATE)
        val body = json.encodeToString(deviceEventResponse)

        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse(body))
        val expectedUrlRequest = createTestRequest()
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(testEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent)
        verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

    }

    @Test
    fun testConsumer_shouldNotDoAnything_whenResponseStatusCodeIs204() = runTest {
        eventClient = createEventClient(backgroundScope)
        eventClient.register()

        everySuspend { mockEmarsysClient.send(any()) }.returns(
            createTestResponse(
                statusCode = HttpStatusCode.NoContent,
                body = ""
            )
        )
        val expectedUrlRequest = createTestRequest()
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        onlineEvents.emit(testEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent)
        verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockOnEventActionFactory.create(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend { mockEventsDao.removeEvent(testEvent) }
    }

    @Test
    fun testConsumer_shouldReEmitEventsToEventFlow_whenOnRecoverCallbackIsCalled() = runTest {
        createEventClient(backgroundScope).register()

        val testException = IOException("No Internet")
        everySuspend { mockEmarsysClient.send(any()) }.calls { args ->
            (args.arg(1) as suspend () -> Unit).invoke()
            throw testException
        }

        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(2).toList()
        }
        val testEvent1 = testEvent.copy(id = "testId1")

        onlineEvents.emit(testEvent)
        onlineEvents.emit(testEvent1)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent, testEvent1)
        verifySuspend { mockEmarsysClient.send(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockOnEventActionFactory.create(any()) }

        // wait for emissions
        delay(5000)
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                "EventClient: Error during event consumption",
                *arrayOf(testEvent)
            )
        }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                "EventClient: Error during event consumption",
                *arrayOf(testEvent1)
            )
        }
        verifySuspend { mockSdkEventManager.emitEvent(testEvent) }
        verifySuspend { mockSdkEventManager.emitEvent(testEvent1) }
    }

    @Test
    fun testConsumer_shouldHandleOnEventActionCampaigns_executeActions_andReportThem_whenPresent() =
        runTest {
            val mockAction = mock<Action<Unit>>() {
                everySuspend { invoke(any()) } returns Unit
            }
            everySuspend { mockOnEventActionFactory.create(any()) } returns mockAction
            eventClient = createEventClient(backgroundScope)
            eventClient.register()

            val html = "testHtml"
            val testInapp = ContentCampaign(InAppType.OVERLAY, TRACKING_INFO, html)
            val expectedInAppMessage =
                InAppMessage(
                    dismissId = UUID,
                    InAppType.OVERLAY,
                    trackingInfo = TRACKING_INFO,
                    content = html
                )
            val action1 = BasicOpenExternalUrlActionModel(
                reporting = "reporting1",
                url = "https://example.com"
            )
            val action2 = BasicAppEventActionModel(
                reporting = "reporting2",
                name = "appEvent",
                payload = mapOf("key" to "value")
            )
            val onEventActionCampaign1 =
                OnEventActionCampaign(
                    trackingInfo = TRACKING_INFO,
                    actions = listOf(action1, action2)
                )

            val action3 = BasicOpenExternalUrlActionModel(
                reporting = "reporting3",
                url = "https://example2.com",
            )
            val onEventActionCampaign2 = OnEventActionCampaign(
                trackingInfo = TRACKING_INFO_1,
                actions = listOf(
                    action3
                )
            )

            val deviceEventResponse = DeviceEventResponse(
                listOf(testInapp),
                listOf(onEventActionCampaign1, onEventActionCampaign2),
                DEVICE_EVENT_STATE
            )
            val body = json.encodeToString(deviceEventResponse)

            everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse(body))
            everySuspend { mockInAppPresenter.present(any(), any(), any()) }.returns(Unit)
            val onEventActionExecutedEventCaptureContainer = Capture.container<SdkEvent.Internal.OnEventActionExecuted>()
            everySuspend {
                mockSdkEventManager.registerEvent(capture(onEventActionExecutedEventCaptureContainer))
            } returns mock()
            val expectedUrlRequest = createTestRequest()
            val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(testEvent)
            advanceUntilIdle()

            onlineSdkEvents.await() shouldBe listOf(testEvent)
            verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
            verifySuspend { mockInAppViewProvider.provide() }
            verifySuspend { mockInAppView.load(expectedInAppMessage) }
            verifySuspend {
                mockOnEventActionFactory.create(action1)
            }
            verifySuspend {
                mockOnEventActionFactory.create(action2)
            }
            verifySuspend {
                mockOnEventActionFactory.create(action3)
            }
            verifySuspend(VerifyMode.exactly(3)) {
                mockSdkEventManager.registerEvent(
                    any()
                )
            }
            onEventActionExecutedEventCaptureContainer.values.size shouldBe 3
            onEventActionExecutedEventCaptureContainer.values[0].trackingInfo shouldBe onEventActionCampaign1.trackingInfo
            onEventActionExecutedEventCaptureContainer.values[0].reporting shouldBe action1.reporting
            onEventActionExecutedEventCaptureContainer.values[1].trackingInfo shouldBe onEventActionCampaign1.trackingInfo
            onEventActionExecutedEventCaptureContainer.values[1].reporting shouldBe action2.reporting
            onEventActionExecutedEventCaptureContainer.values[2].trackingInfo shouldBe onEventActionCampaign2.trackingInfo
            onEventActionExecutedEventCaptureContainer.values[2].reporting shouldBe action3.reporting

            verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
        }

    @Test
    fun testConsumer_shouldExecuteAllActions_whenSomeActionsFail() =
        runTest {
            val mockAction = mock<Action<Unit>> {
                everySuspend { invoke(any()) } returns Unit
            }
            val mockFailingAppEventAction = mock<Action<Unit>> {
                everySuspend { invoke(any()) } throws Exception("This action fails.")
            }
            everySuspend { mockOnEventActionFactory.create(any()) } calls { args ->
                if (args.arg<ActionModel>(0) is BasicOpenExternalUrlActionModel) {
                    mockAction
                } else {
                    mockFailingAppEventAction
                }
            }
            eventClient = createEventClient(backgroundScope)
            eventClient.register()

            val action1 = BasicOpenExternalUrlActionModel(
                reporting = "reporting1",
                url = "https://example.com",
            )
            val action2 = BasicAppEventActionModel(
                reporting = "reporting2",
                name = "appEvent",
                payload = mapOf("key" to "value")
            )
            val action3 = BasicOpenExternalUrlActionModel(
                reporting = "reporting3",
                url = "https://example3.com"
            )
            val onEventActionCampaign1 =
                OnEventActionCampaign(
                    trackingInfo = TRACKING_INFO,
                    actions = listOf(action1, action2, action3)
                )

            val action4 = BasicOpenExternalUrlActionModel(
                reporting = "reporting4",
                url = "https://example4.com",
            )
            val onEventActionCampaign2 = OnEventActionCampaign(
                trackingInfo = TRACKING_INFO_1,
                actions = listOf(action4)
            )

            val deviceEventResponse = DeviceEventResponse(
                null,
                listOf(onEventActionCampaign1, onEventActionCampaign2),
                DEVICE_EVENT_STATE
            )
            val body = json.encodeToString(deviceEventResponse)

            everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse(body))
            val onEventActionExecutedEventCaptureContainer = Capture.container<SdkEvent.Internal.OnEventActionExecuted>()
            everySuspend {
                mockSdkEventManager.registerEvent(capture(onEventActionExecutedEventCaptureContainer))
            } returns mock()
            val expectedUrlRequest = createTestRequest()
            val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
                onlineEvents.take(1).toList()
            }

            onlineEvents.emit(testEvent)
            advanceUntilIdle()

            onlineSdkEvents.await() shouldBe listOf(testEvent)
            verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
            verifySuspend {
                mockOnEventActionFactory.create(action1)
            }
            verifySuspend {
                mockOnEventActionFactory.create(action2)
            }
            verifySuspend {
                mockOnEventActionFactory.create(action3)
            }
            verifySuspend {
                mockOnEventActionFactory.create(action4)
            }
            verifySuspend(VerifyMode.exactly(3)) {
                mockSdkEventManager.registerEvent(
                    any()
                )
            }
            onEventActionExecutedEventCaptureContainer.values.size shouldBe 3
            onEventActionExecutedEventCaptureContainer.values[0].trackingInfo shouldBe onEventActionCampaign1.trackingInfo
            onEventActionExecutedEventCaptureContainer.values[0].reporting shouldBe action1.reporting
            onEventActionExecutedEventCaptureContainer.values[1].trackingInfo shouldBe onEventActionCampaign1.trackingInfo
            onEventActionExecutedEventCaptureContainer.values[1].reporting shouldBe action3.reporting
            onEventActionExecutedEventCaptureContainer.values[2].trackingInfo shouldBe onEventActionCampaign2.trackingInfo
            onEventActionExecutedEventCaptureContainer.values[2].reporting shouldBe action4.reporting

            verifySuspend(VerifyMode.exactly(1)) { mockSdkLogger.error(any(), any<Throwable>()) }
        }

    @Test
    fun testConsumer_shouldCallClientExceptionHandler_whenExceptionIsThrown() = runTest {
        createEventClient(backgroundScope).register()
        val testException = Exception("Test Exception")

        everySuspend { mockEmarsysClient.send(any()) }.calls { args ->
            (args.arg(1) as suspend () -> Unit).invoke()
            throw testException
        }

        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(2).toList()
        }
        val testEvent1 = testEvent.copy(id = "testId1")

        onlineEvents.emit(testEvent)
        onlineEvents.emit(testEvent1)

        advanceUntilIdle()

        delay(1000)
        onlineSdkEvents.await() shouldBe listOf(testEvent, testEvent1)
        verifySuspend { mockEmarsysClient.send(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockOnEventActionFactory.create(any()) }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                "EventClient: Error during event consumption",
                *arrayOf(testEvent)
            )
        }
        verifySuspend {
            mockClientExceptionHandler.handleException(
                testException,
                "EventClient: Error during event consumption",
                *arrayOf(testEvent1)
            )
        }
    }


    private fun createTestRequest(): UrlRequest {
        val expectedUrlRequest = UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post,
            """{"dnd":$IN_APP_DND,"events":[{"type":"custom","name":"${testEvent.name}","timestamp":"$TIMESTAMP","attributes":{"key":"value","key1":"5"}}]}""",
        )
        return expectedUrlRequest
    }

    private fun createTestResponse(
        body: String = """{ "deviceEventState": "$DEVICE_EVENT_STATE" }""",
        statusCode: HttpStatusCode = HttpStatusCode.OK
    ) = Response(
        UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post
        ), statusCode, Headers.Empty, bodyAsText = body
    )
}