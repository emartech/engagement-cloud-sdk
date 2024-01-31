package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import com.emarsys.providers.Provider
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

class EventTrackerInternalTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    private companion object {
        val timestamp = Clock.System.now()
        val customEvent = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
        val event = Event(EventType.CUSTOM, "testEvent", mapOf("testAttribute" to "testValue"), timestamp.toString())
        val event2 =
            Event(EventType.INTERNAL, "testEvent2", mapOf("testAttribute2" to "testValue2"), timestamp.toString())
        val trackEvent = EventTrackerCall.TrackEvent(event)
        val trackEvent2 = EventTrackerCall.TrackEvent(event2)
        val expectedEvents: MutableList<EventTrackerCall> = mutableListOf(trackEvent, trackEvent2)
    }

    @Mock
    lateinit var mockEventClient: EventClientApi

    @Mock
    lateinit var mockTimestampProvider: Provider<Instant>

    private lateinit var eventTrackerContext: ApiContext<EventTrackerCall>

    private val eventTrackerInternal: EventTrackerInstance by withMocks {
        every { mockTimestampProvider.provide() } returns timestamp
        eventTrackerContext = EventTrackerContext(expectedEvents)
        EventTrackerInternal(mockEventClient, eventTrackerContext, mockTimestampProvider)
    }


    @Test
    fun testTrackEvent_shouldMakeCall_onClient() = runTest {
        everySuspending { mockEventClient.registerEvent(event) } returns Unit

        eventTrackerInternal.trackEvent(customEvent)

        verifyWithSuspend {
            mockTimestampProvider.provide()
            mockEventClient.registerEvent(event)
        }
    }

    @Test
    fun testActivate_should_send_calls_to_client() = runTest {
        everySuspending { mockEventClient.registerEvent(event) } returns Unit
        everySuspending { mockEventClient.registerEvent(event2) } returns Unit

        eventTrackerInternal.activate()

        verifyWithSuspend {
            mockEventClient.registerEvent(event)
            mockEventClient.registerEvent(event2)
        }
    }
}