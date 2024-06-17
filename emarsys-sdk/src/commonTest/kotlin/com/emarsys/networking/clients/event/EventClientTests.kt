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
import com.emarsys.mobileengage.action.models.OnEventActionModel
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
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

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventClientTests {
    private companion object {
        const val DEVICE_EVENT_STATE = "test device event state"
        const val EVENT_NAME = "test event name"
        const val IN_APP_DND = false
        const val TIMESTAMP = "testTimestamp"
        val testEventAttributes = mapOf("key" to "value")
        val testEvent = Event(EventType.CUSTOM, EVENT_NAME, testEventAttributes, TIMESTAMP)
    }

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    private lateinit var mockEmarsysClient: NetworkClientApi
    private lateinit var mockUrlFactory: UrlFactoryApi
    private lateinit var mockOnEventActionFactory: ActionFactoryApi<OnEventActionModel>
    private lateinit var mockDeviceEventChannel: DeviceEventChannelApi
    private lateinit var mockInAppConfig: InAppConfig
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

        json = Json {
            encodeDefaults = true
        }
        sdkDispatcher = StandardTestDispatcher()
        sessionContext = SessionContext(deviceEventState = DEVICE_EVENT_STATE)
        every { mockInAppConfig.inAppDnd }.returns(IN_APP_DND)

        everySuspend { mockDeviceEventChannel.consume() }.returns(flowOf(testEvent))
        everySuspend { mockDeviceEventChannel.send(testEvent) }.returns(Unit)
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
            mockOnEventActionFactory,
            sessionContext,
            mockInAppConfig,
            sdkDispatcher
        )

        eventClient.registerEvent(testEvent)

        verifySuspend {
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
            mockOnEventActionFactory,
            sessionContext,
            mockInAppConfig,
            sdkDispatcher
        )

        advanceUntilIdle()

        verifySuspend {
            mockDeviceEventChannel.consume()
        }
    }

    @Test
    fun testConsumer_should_call_client_with_correct_request() = runTest {
        val testBaseUrl = Url("https://test-base-url/")

        everySuspend { mockEmarsysClient.send(any()) }.returns(
            Response(
                UrlRequest(
                    testBaseUrl,
                    HttpMethod.Post
                ), HttpStatusCode.OK, Headers.Empty, ""
            )
        )
        every { mockUrlFactory.create(EmarsysUrlType.EVENT) }.returns(testBaseUrl)

        val expectedUrlRequest = UrlRequest(
            testBaseUrl,
            HttpMethod.Post,
            """{"dnd":$IN_APP_DND,"events":[{"type":"${testEvent.type}","name":"${testEvent.name}","attributes":{"key":"value"},"timestamp":"$TIMESTAMP"}],"deviceEventState":"$DEVICE_EVENT_STATE"}""",
        )

        eventClient = EventClient(
            mockEmarsysClient,
            mockUrlFactory,
            json,
            mockDeviceEventChannel,
            mockOnEventActionFactory,
            sessionContext,
            mockInAppConfig,
            sdkDispatcher
        )

        eventClient.registerEvent(testEvent)

        advanceUntilIdle()

        verifySuspend {
            mockEmarsysClient.send(expectedUrlRequest)
        }
    }
}