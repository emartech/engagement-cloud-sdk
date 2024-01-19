package com.emarsys.clients.event

import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.DeviceEventChannelApi
import com.emarsys.core.networking.clients.NetworkClientApi

import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.networking.clients.event.EventClient
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import com.emarsys.session.SessionContext
import com.emarsys.url.UrlFactoryApi
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
import kotlinx.serialization.json.Json
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventClientTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    private companion object {
        const val DEVICE_EVENT_STATE = "test device event state"
        const val EVENT_NAME = "test event name"
        const val IN_APP_DND = false
        val testEventAttributes = mapOf("key" to "value")
        val testEvent = Event(EventType.CUSTOM, EVENT_NAME, testEventAttributes)
    }

    @Mock
    lateinit var mockEmarsysClient: NetworkClientApi

    @Mock
    lateinit var mockUrlFactory: UrlFactoryApi

    @Mock
    lateinit var mockDeviceEventChannel: DeviceEventChannelApi

    @Mock
    lateinit var mockSdkContext: SdkContextApi

    private lateinit var sdkDispatcher: CoroutineDispatcher

    private lateinit var sessionContext: SessionContext

    private lateinit var json: Json

    private lateinit var eventClient: EventClient

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        json = Json {
            encodeDefaults = true
        }
        sdkDispatcher = StandardTestDispatcher()
        sessionContext = SessionContext(deviceEventState = DEVICE_EVENT_STATE)
        every { mockSdkContext.inAppDndD }.returns(IN_APP_DND)

        everySuspending { mockDeviceEventChannel.consume() }.returns(flowOf(testEvent))
        everySuspending { mockDeviceEventChannel.send(testEvent) }.runs { }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRegisterEvent_should_send_event_to_channel() = runTest {
        eventClient = EventClient(
            mockEmarsysClient,
            mockUrlFactory,
            json,
            mockDeviceEventChannel,
            sessionContext,
            mockSdkContext,
            sdkDispatcher
        )

        eventClient.registerEvent(testEvent)

        verifyWithSuspend(exhaustive = false) {
            mockDeviceEventChannel.send(testEvent)
        }
    }

    @Test
    fun testInit_should_start_consume_on_channel() = runTest {
        eventClient = EventClient(
            mockEmarsysClient,
            mockUrlFactory,
            json,
            mockDeviceEventChannel,
            sessionContext,
            mockSdkContext,
            sdkDispatcher
        )

        advanceUntilIdle()

        verifyWithSuspend(exhaustive = false) {
            mockDeviceEventChannel.consume()
        }
    }

    @Test
    fun testConsumer_should_call_client_with_correct_request() = runTest {
        val testBaseUrl = Url("https://test-base-url/")

        everySuspending { mockEmarsysClient.send(isAny()) }.returns(
            Response(
                UrlRequest(
                    testBaseUrl,
                    HttpMethod.Post
                ), HttpStatusCode.OK, Headers.Empty, ""
            )
        )
        every { mockUrlFactory.create(isAny()) }.returns(testBaseUrl)

        val expectedUrlRequest = UrlRequest(
            testBaseUrl,
            HttpMethod.Post,
            """{"dnd":$IN_APP_DND,"events":[{"eventType":"${testEvent.eventType}","eventName":"${testEvent.eventName}","attributes":{"key":"value"}}],"deviceEventState":"$DEVICE_EVENT_STATE"}""",
            )

        eventClient = EventClient(
            mockEmarsysClient,
            mockUrlFactory,
            json,
            mockDeviceEventChannel,
            sessionContext,
            mockSdkContext,
            sdkDispatcher
        )

        eventClient.registerEvent(testEvent)

        advanceUntilIdle()

        verifyWithSuspend(exhaustive = false) {
            mockEmarsysClient.send(expectedUrlRequest)
        }
    }

}