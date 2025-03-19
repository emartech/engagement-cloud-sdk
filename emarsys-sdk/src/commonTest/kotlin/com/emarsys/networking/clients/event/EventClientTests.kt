package com.emarsys.networking.clients.event

import com.emarsys.api.inapp.InAppConfig
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.session.SessionContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppViewApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.WebViewHolder
import com.emarsys.networking.clients.event.model.DeviceEventResponse
import com.emarsys.networking.clients.event.model.EventResponseInApp
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventClientTests {
    private companion object {
        val DEVICE_EVENT_STATE = JsonObject(mapOf("key" to JsonPrimitive("value")))
        const val EVENT_NAME = "test event name"
        const val UUID = "testUuid"
        const val IN_APP_DND = false
        val TIMESTAMP = Clock.System.now()
        const val CAMPAIGN_ID = "testTimestamp"
        val TEST_BASE_URL = Url("https://test-base-url/")
        val testEventAttributes = buildJsonObject { put("key", JsonPrimitive("value")) }
        val testEvent = SdkEvent.External.Custom(
            id = UUID,
            name = EVENT_NAME,
            attributes = testEventAttributes,
            timestamp = TIMESTAMP
        )
    }

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockOnEventActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var sdkEventFlow: MutableSharedFlow<SdkEvent>
    private lateinit var onlineSdkEvents: MutableSharedFlow<SdkEvent>
    private lateinit var mockInAppConfig: InAppConfig
    private lateinit var mockInAppPresenter: InAppPresenterApi
    private lateinit var mockInAppViewProvider: InAppViewProviderApi
    private lateinit var mockInAppView: InAppViewApi
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var sessionContext: SessionContext
    private lateinit var json: Json
    private lateinit var eventClient: EventClient
    private lateinit var mockSdkLogger: Logger
    private lateinit var mockWebViewHolder: WebViewHolder

    @BeforeTest
    fun setup() = runTest {
        mockEmarsysClient = mock()
        mockUrlFactory = mock()
        mockOnEventActionFactory = mock()
        sdkEventFlow = spy(MutableSharedFlow(replay = 5))
        onlineSdkEvents = spy(MutableSharedFlow(replay = 5))
        mockInAppConfig = mock()
        mockInAppPresenter = mock()
        mockInAppViewProvider = mock()
        mockInAppView = mock()
        mockSdkLogger = mock(MockMode.autofill)
        json = JsonUtil.json
        mockWebViewHolder = mock()
        sdkDispatcher =
            StandardTestDispatcher()
        sessionContext = SessionContext()
        every { mockInAppConfig.inAppDnd }.returns(IN_APP_DND)
        every { mockUrlFactory.create(EmarsysUrlType.EVENT, null) }.returns(TEST_BASE_URL)
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
            throw it.args[1] as Throwable
        }
        everySuspend { mockInAppViewProvider.provide() } returns mockInAppView
        everySuspend { mockInAppView.load(any()) } returns mockWebViewHolder
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRegisterEvent_should_send_event_to_channel() = runTest {
        eventClient = createEventClient()

        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse("{}"))

        sdkEventFlow.emit(testEvent)

        verifySuspend { sdkEventFlow.emit(testEvent) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

    }

    @Test
    fun testInit_should_start_consume_on_channel() = runTest {
        eventClient = createEventClient()

        advanceUntilIdle()

        verifySuspend { sdkEventFlow.filter(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

    }

    @Test
    fun testConsumer_should_call_client_with_correct_request() = runTest {
        sessionContext.deviceEventState = DEVICE_EVENT_STATE

        everySuspend { mockEmarsysClient.send(any(), any()) }.returns(createTestResponse("{}"))
        every { mockUrlFactory.create(EmarsysUrlType.EVENT, null) }.returns(TEST_BASE_URL)

        val expectedUrlRequest = UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post,
            """{"dnd":$IN_APP_DND,"events":[{"fullClassName":"com.emarsys.networking.clients.event.model.SdkEvent.External.Custom","type":"custom","id":"$UUID","name":"${testEvent.name}","attributes":{"key":"value"},"timestamp":"$TIMESTAMP"}],"deviceEventState":{"key":"value"}}""",
        )

        eventClient = createEventClient()

        onlineSdkEvents.emit(testEvent)

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

    }

    @Test
    fun testConsumer_shouldHandleDeviceEventState() = runTest {
        sessionContext.deviceEventState = null
        val expectedDeviceEventState = buildJsonObject {
            put("key1", "value1")
            put("key2", "value2")
        }

        val deviceEventResponse = DeviceEventResponse(null, null, expectedDeviceEventState)
        val responseBody = json.encodeToString(deviceEventResponse)

        everySuspend { mockEmarsysClient.send(any(), any()) }.returns(
            createTestResponse(
                responseBody
            )
        )
        every { mockUrlFactory.create(EmarsysUrlType.EVENT, null) }.returns(TEST_BASE_URL)

        val expectedUrlRequest = createTestRequest()

        eventClient = createEventClient()

        onlineSdkEvents.emit(testEvent)

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

        sessionContext.deviceEventState shouldBe expectedDeviceEventState
    }

    @Test
    fun testConsumer_shouldHandleInApp_whenHtml_isPresent() = runTest {
        val html = "testHtml"
        val testInapp = EventResponseInApp(CAMPAIGN_ID, html)
        val expectedInAppMessage = InAppMessage(CAMPAIGN_ID, html)
        val deviceEventResponse = DeviceEventResponse(testInapp, null, null)
        val body = json.encodeToString(deviceEventResponse)

        everySuspend { mockEmarsysClient.send(any(), any()) }.returns(createTestResponse(body))
        everySuspend { mockInAppPresenter.present(any(), any(), any()) }.returns(Unit)

        val expectedUrlRequest = createTestRequest()

        eventClient = createEventClient()

        onlineSdkEvents.emit(testEvent)
        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) }
        verifySuspend { mockInAppViewProvider.provide() }
        verifySuspend { mockInAppView.load(expectedInAppMessage) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    @Test
    fun testConsumer_shouldNotHandleInApp_whenHtml_IsEmpty() = runTest {
        val html = ""
        val testInapp = EventResponseInApp(CAMPAIGN_ID, html)
        val deviceEventResponse = DeviceEventResponse(testInapp, null, null)
        val body = json.encodeToString(deviceEventResponse)

        everySuspend { mockEmarsysClient.send(any(), any()) }.returns(createTestResponse(body))

        val expectedUrlRequest = createTestRequest()

        eventClient = createEventClient()

        onlineSdkEvents.emit(testEvent)

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

    }

    @Test
    fun testConsumer_shouldNotDoAnything_whenResponseStatusCodeIs204() = runTest {
        everySuspend { mockEmarsysClient.send(any(), any()) }.returns(
            createTestResponse(
                statusCode = HttpStatusCode.NoContent,
                body = ""
            )
        )
        val expectedUrlRequest = createTestRequest()

        eventClient = createEventClient()

        onlineSdkEvents.emit(testEvent)

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockOnEventActionFactory.create(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

        sessionContext.deviceEventState shouldBe null
    }

    @Test
    fun testConsumer_shouldReEmitEventsToEventFlow_whenOnRecoverCallbackIsCalled() = runTest {
        everySuspend { mockEmarsysClient.send(any(), any()) }.calls { args ->
            (args.arg(1) as suspend () -> Unit).invoke()
            throw Exception("testException")
        }

        eventClient = createEventClient()

        val sdkEventFlowEvents = backgroundScope.async {
            sdkEventFlow.take(2).toList()
        }
        val testEvent1 = testEvent.copy(id = "testId1")

        onlineSdkEvents.emitAll(flowOf(testEvent, testEvent1))

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockOnEventActionFactory.create(any()) }

        sessionContext.deviceEventState shouldBe null

        sdkEventFlowEvents.await() shouldBe listOf(testEvent, testEvent1)
        verifySuspend { mockSdkLogger.error(any(), any<Throwable>()) }
    }


    private fun createTestRequest(): UrlRequest {
        val expectedUrlRequest = UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post,
            """{"dnd":$IN_APP_DND,"events":[{"fullClassName":"com.emarsys.networking.clients.event.model.SdkEvent.External.Custom","type":"custom","id":"$UUID","name":"${testEvent.name}","attributes":{"key":"value"},"timestamp":"$TIMESTAMP"}]}""",
        )
        return expectedUrlRequest
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

    private fun createEventClient() = EventClient(
        mockEmarsysClient,
        mockUrlFactory,
        json,
        mockOnEventActionFactory,
        sessionContext,
        mockInAppConfig,
        mockInAppPresenter,
        mockInAppViewProvider,
        sdkEventFlow,
        onlineSdkEvents,
        mockSdkLogger,
        sdkDispatcher
    )
}