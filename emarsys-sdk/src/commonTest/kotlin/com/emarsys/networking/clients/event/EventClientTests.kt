package com.emarsys.networking.clients.event

import com.emarsys.api.inapp.InAppConfigApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
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
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.InAppPresenterApi
import com.emarsys.mobileengage.inapp.InAppType
import com.emarsys.mobileengage.inapp.InAppViewApi
import com.emarsys.mobileengage.inapp.InAppViewProviderApi
import com.emarsys.mobileengage.inapp.WebViewHolder
import com.emarsys.networking.clients.event.model.DeviceEventResponse
import com.emarsys.networking.clients.event.model.EventResponseInApp
import com.emarsys.util.JsonUtil
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okio.IOException
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
        const val CAMPAIGN_ID = "testCampaignId"
        val TEST_BASE_URL = Url("https://test-base-url/")
        val testEventAttributes = buildJsonObject { put("key", JsonPrimitive("value")) }
        val testEvent = SdkEvent.External.Custom(
            id = UUID,
            name = EVENT_NAME,
            attributes = testEventAttributes,
            timestamp = TIMESTAMP
        )
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
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
        everySuspend { mockSdkEventManager.registerEvent(any()) } calls {
            onlineEvents.emit(it.args[0] as OnlineSdkEvent)
            mock(MockMode.autofill)
        }
        every { mockInAppConfigApi.inAppDnd }.returns(IN_APP_DND)
        every { mockUrlFactory.create(EmarsysUrlType.EVENT) }.returns(TEST_BASE_URL)
        everySuspend { mockSdkLogger.error(any(), any<Throwable>()) } calls {
            (it.args[1] as Throwable).printStackTrace()
        }
        everySuspend { mockInAppViewProvider.provide() } returns mockInAppView
        everySuspend { mockInAppView.load(any()) } returns mockWebViewHolder
    }

    private fun createEventClient(applicationScope: CoroutineScope) = EventClient(
        mockEmarsysClient,
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

        everySuspend { mockEmarsysClient.send(any(), any()) } returns createTestResponse("{}")
        every { mockUrlFactory.create(EmarsysUrlType.EVENT) } returns TEST_BASE_URL
        val expectedUrlRequest = UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post,
            """{"dnd":$IN_APP_DND,"events":[{"fullClassName":"com.emarsys.event.SdkEvent.External.Custom","type":"custom","id":"$UUID","name":"${testEvent.name}","attributes":{"key":"value"},"timestamp":"$TIMESTAMP","nackCount":0}],"deviceEventState":{"key":"value"}}""",
        )
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        mockSdkEventManager.registerEvent(testEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent)
        verifySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    @Test
    fun testConsumer_shouldHandleDeviceEventState() = runTest {
        eventClient = createEventClient(backgroundScope)
        eventClient.register()

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
        every { mockUrlFactory.create(EmarsysUrlType.EVENT) }.returns(TEST_BASE_URL)
        val expectedUrlRequest = createTestRequest()
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        mockSdkEventManager.registerEvent(testEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent)
        verifySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

        verify { mockRequestContext.deviceEventState = expectedDeviceEventState }
    }

    @Test
    fun testConsumer_shouldHandleInApp_whenHtml_isPresent() = runTest {
        eventClient = createEventClient(backgroundScope)
        eventClient.register()

        val html = "testHtml"
        val testInapp = EventResponseInApp(CAMPAIGN_ID, html)
        val expectedInAppMessage =
            InAppMessage(
                dismissId = UUID,
                InAppType.OVERLAY,
                trackingInfo = CAMPAIGN_ID,
                content = html
            )
        val deviceEventResponse = DeviceEventResponse(testInapp, null, null)
        val body = json.encodeToString(deviceEventResponse)

        everySuspend { mockEmarsysClient.send(any(), any()) }.returns(createTestResponse(body))
        everySuspend { mockInAppPresenter.present(any(), any(), any()) }.returns(Unit)
        val expectedUrlRequest = createTestRequest()
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        mockSdkEventManager.registerEvent(testEvent)
        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent)
        verifySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) }
        verifySuspend { mockInAppViewProvider.provide() }
        verifySuspend { mockInAppView.load(expectedInAppMessage) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
    }

    @Test
    fun testConsumer_shouldNotHandleInApp_whenHtml_IsEmpty() = runTest {
        eventClient = createEventClient(backgroundScope)
        eventClient.register()

        val html = ""
        val testInapp = EventResponseInApp(CAMPAIGN_ID, html)
        val deviceEventResponse = DeviceEventResponse(testInapp, null, null)
        val body = json.encodeToString(deviceEventResponse)

        everySuspend { mockEmarsysClient.send(any(), any()) }.returns(createTestResponse(body))
        val expectedUrlRequest = createTestRequest()
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        mockSdkEventManager.registerEvent(testEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent)
        verifySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }

    }

    @Test
    fun testConsumer_shouldNotDoAnything_whenResponseStatusCodeIs204() = runTest {
        eventClient = createEventClient(backgroundScope)
        eventClient.register()

        everySuspend { mockEmarsysClient.send(any(), any()) }.returns(
            createTestResponse(
                statusCode = HttpStatusCode.NoContent,
                body = ""
            )
        )
        val expectedUrlRequest = createTestRequest()
        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(1).toList()
        }

        mockSdkEventManager.registerEvent(testEvent)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent)
        verifySuspend { mockEmarsysClient.send(expectedUrlRequest, any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockOnEventActionFactory.create(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend { mockEventsDao.removeEvent(testEvent) }
    }

    @Test
    fun testConsumer_shouldReEmitEventsToEventFlow_whenOnRecoverCallbackIsCalled() = runTest {
        createEventClient(backgroundScope).register()

        everySuspend { mockEmarsysClient.send(any(), any()) }.calls { args ->
            (args.arg(1) as suspend () -> Unit).invoke()
            throw IOException("No Internet")
        }

        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(2).toList()
        }
        val testEvent1 = testEvent.copy(id = "testId1")

        mockSdkEventManager.registerEvent(testEvent)
        mockSdkEventManager.registerEvent(testEvent1)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent, testEvent1)
        verifySuspend { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockOnEventActionFactory.create(any()) }

        // wait for emissions
        delay(5000)
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(testEvent) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(testEvent1) }
        verifySuspend { mockSdkEventManager.emitEvent(testEvent) }
        verifySuspend { mockSdkEventManager.emitEvent(testEvent1) }
    }

    @Test
    fun testConsumer_shouldNotAckMessages_whenUnknownExceptionIsThrown() = runTest {
        eventClient = createEventClient(backgroundScope)
        eventClient.register()

        everySuspend { mockEmarsysClient.send(any(), any()) }.calls { args ->
            (args.arg(1) as suspend () -> Unit).invoke()
            throw IOException("testException")
        }

        val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
            onlineEvents.take(2).toList()
        }
        val testEvent1 = testEvent.copy(id = "testId1")

        mockSdkEventManager.registerEvent(testEvent)
        mockSdkEventManager.registerEvent(testEvent1)

        advanceUntilIdle()

        onlineSdkEvents.await() shouldBe listOf(testEvent, testEvent1)
        verifySuspend { mockSdkLogger.error(any(), any<Throwable>()) }
        verifySuspend { mockEmarsysClient.send(any(), any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
        verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockOnEventActionFactory.create(any()) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(testEvent) }
        verifySuspend(VerifyMode.exactly(0)) { mockEventsDao.removeEvent(testEvent1) }
    }

    @Test
    fun testConsumer_shouldAckEvents_whenKnownExceptionIsThrown() {
        forAll(
            table(
                headers("exception"),
                listOf(
                    row(
                        FailedRequestException(
                            response = createTestResponse()
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
                createEventClient(backgroundScope).register()

                everySuspend { mockEmarsysClient.send(any(), any()) }.calls { args ->
                    (args.arg(1) as suspend () -> Unit).invoke()
                    throw testException
                }

                val onlineSdkEvents = backgroundScope.async(start = CoroutineStart.UNDISPATCHED) {
                    onlineEvents.take(2).toList()
                }
                val testEvent1 = testEvent.copy(id = "testId1")

                mockSdkEventManager.registerEvent(testEvent)
                mockSdkEventManager.registerEvent(testEvent1)

                advanceUntilIdle()

                delay(1000)
                onlineSdkEvents.await() shouldBe listOf(testEvent, testEvent1)
                verifySuspend(VerifyMode.exactly(0)) {
                    mockSdkLogger.error(
                        any(),
                        any<Throwable>()
                    )
                }
                verifySuspend { mockEmarsysClient.send(any(), any()) }
                verifySuspend(VerifyMode.exactly(0)) { mockInAppViewProvider.provide() }
                verifySuspend(VerifyMode.exactly(0)) { mockInAppView.load(any()) }
                verifySuspend(VerifyMode.exactly(0)) { mockOnEventActionFactory.create(any()) }
                verifySuspend { mockEventsDao.removeEvent(testEvent) }
                verifySuspend { mockEventsDao.removeEvent(testEvent1) }
            }
        }
    }


    private fun createTestRequest(): UrlRequest {
        val expectedUrlRequest = UrlRequest(
            TEST_BASE_URL,
            HttpMethod.Post,
            """{"dnd":$IN_APP_DND,"events":[{"fullClassName":"com.emarsys.event.SdkEvent.External.Custom","type":"custom","id":"$UUID","name":"${testEvent.name}","attributes":{"key":"value"},"timestamp":"$TIMESTAMP","nackCount":0}]}""",
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
}