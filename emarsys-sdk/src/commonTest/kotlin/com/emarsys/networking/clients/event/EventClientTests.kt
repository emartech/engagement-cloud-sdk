package com.emarsys.networking.clients.event

import com.emarsys.api.inapp.InAppConfig
import com.emarsys.core.channel.DeviceEventChannelApi
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
import com.emarsys.networking.clients.event.model.DeviceEventResponse
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventResponseInApp
import com.emarsys.networking.clients.event.model.EventType
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventClientTests {
    private companion object {
        val DEVICE_EVENT_STATE = buildJsonObject { }
        const val EVENT_NAME = "test event name"
        const val IN_APP_DND = false
        const val TIMESTAMP = "testTimestamp"
        const val CAMPAIGN_ID = "testTimestamp"
        val TEST_BASE_URL = Url("https://test-base-url/")
        val testEventAttributes = mapOf("key" to "value")
        val testEvent = Event(EventType.CUSTOM, EVENT_NAME, testEventAttributes, TIMESTAMP)
    }

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockOnEventActionFactory: ActionFactoryApi<ActionModel>
    private lateinit var mockDeviceEventChannel: DeviceEventChannelApi
    private lateinit var mockInAppConfig: InAppConfig
    private lateinit var mockInAppPresenter: InAppPresenterApi
    private lateinit var mockInAppViewProvider: InAppViewProviderApi
    private lateinit var mockInAppView: InAppViewApi
    private lateinit var sdkDispatcher: CoroutineDispatcher
    private lateinit var sessionContext: SessionContext
    private lateinit var json: Json
    private lateinit var eventClient: EventClient

    @BeforeTest
    fun setup() = runTest {
        mockEmarsysClient = mock()
        mockUrlFactory = mock()
        mockOnEventActionFactory = mock()
        mockDeviceEventChannel = mock()
        mockInAppConfig = mock()
        mockInAppPresenter = mock()
        mockInAppViewProvider = mock()
        mockInAppView = mock()

        json = JsonUtil.json
        sdkDispatcher = StandardTestDispatcher()
        sessionContext = SessionContext()
        every { mockInAppConfig.inAppDnd }.returns(IN_APP_DND)
        every { mockUrlFactory.create(EmarsysUrlType.EVENT) }.returns(TEST_BASE_URL)
        everySuspend { mockInAppViewProvider.provide() } returns mockInAppView
        everySuspend { mockInAppView.load(any()) } returns Unit

        everySuspend { mockDeviceEventChannel.consume() }.returns(flowOf(testEvent))
        everySuspend { mockDeviceEventChannel.send(testEvent) }.returns(Unit)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRegisterEvent_should_send_event_to_channel() = runTest {
        eventClient = createEventClient()

        eventClient.registerEvent(testEvent)

        verifySuspend { mockDeviceEventChannel.send(testEvent) }
    }

    @Test
    fun testInit_should_start_consume_on_channel() = runTest {
        eventClient = createEventClient()

        advanceUntilIdle()

        verifySuspend { mockDeviceEventChannel.consume() }
    }

    @Test
    fun testConsumer_should_call_client_with_correct_request() = runTest {
        sessionContext.deviceEventState = DEVICE_EVENT_STATE

        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse())
        every { mockUrlFactory.create(EmarsysUrlType.EVENT) }.returns(TEST_BASE_URL)

        val expectedUrlRequest = createTestRequest(DEVICE_EVENT_STATE)

        eventClient = createEventClient()

        eventClient.registerEvent(testEvent)

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
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

        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse(responseBody))
        every { mockUrlFactory.create(EmarsysUrlType.EVENT) }.returns(TEST_BASE_URL)

        val expectedUrlRequest = createTestRequest(null)

        eventClient = createEventClient()

        eventClient.registerEvent(testEvent)

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
        sessionContext.deviceEventState shouldBe expectedDeviceEventState
    }

    @Test
    fun testConsumer_shouldHandleInApp_whenHtml_isPresent() = runTest {
        val html = "testHtml"
        val testInapp = EventResponseInApp(CAMPAIGN_ID, html)
        val expectedInAppMessage = InAppMessage(html)
        val deviceEventResponse = DeviceEventResponse(testInapp, null, null)
        val body = json.encodeToString(deviceEventResponse)

        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse(body))

        val expectedUrlRequest = createTestRequest(null)

        eventClient = createEventClient()

        eventClient.registerEvent(testEvent)

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
        verifySuspend { mockInAppViewProvider.provide() }
        verifySuspend { mockInAppView.load(expectedInAppMessage) }
    }

    @Test
    fun testConsumer_shouldNotHandleInApp_whenHtml_IsEmpty() = runTest {
        val html = ""
        val testInapp = EventResponseInApp(CAMPAIGN_ID, html)
        val deviceEventResponse = DeviceEventResponse(testInapp, null, null)
        val body = json.encodeToString(deviceEventResponse)

        everySuspend { mockEmarsysClient.send(any()) }.returns(createTestResponse(body))

        val expectedUrlRequest = createTestRequest(null)

        eventClient = createEventClient()

        eventClient.registerEvent(testEvent)

        advanceUntilIdle()

        verifySuspend { mockEmarsysClient.send(expectedUrlRequest) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
    }

    private fun createTestRequest(deviceEventState: JsonObject? = null): UrlRequest {
        val expectedUrlRequest = UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post,
            """{"dnd":$IN_APP_DND,"events":[{"type":"${testEvent.type}","name":"${testEvent.name}","attributes":{"key":"value"},"timestamp":"$TIMESTAMP"}],"deviceEventState":$deviceEventState}""",
        )
        return expectedUrlRequest
    }

    private fun createTestResponse(body: String = "") = Response(
        UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post
        ), HttpStatusCode.OK, Headers.Empty, bodyAsText = body
    )

    private fun createEventClient() = EventClient(
        mockEmarsysClient,
        mockUrlFactory,
        json,
        mockDeviceEventChannel,
        mockOnEventActionFactory,
        sessionContext,
        mockInAppConfig,
        mockInAppPresenter,
        mockInAppViewProvider,
        sdkDispatcher
    )
}