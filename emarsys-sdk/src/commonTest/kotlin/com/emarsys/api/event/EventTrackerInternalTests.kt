package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.core.providers.Provider
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test

class EventTrackerInternalTests {
    private companion object {
        val timestamp = Clock.System.now()
        val customEvent = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
        val event = Event(
            EventType.CUSTOM,
            "testEvent",
            mapOf("testAttribute" to "testValue"),
            timestamp.toString()
        )
        val event2 =
            Event(
                EventType.INTERNAL,
                "testEvent2",
                mapOf("testAttribute2" to "testValue2"),
                timestamp.toString()
            )
        val trackEvent = EventTrackerCall.TrackEvent(event)
        val trackEvent2 = EventTrackerCall.TrackEvent(event2)
        val expectedEvents: MutableList<EventTrackerCall> = mutableListOf(trackEvent, trackEvent2)
    }

    private lateinit var mockEventClient: EventClientApi
    private lateinit var mockTimestampProvider: Provider<Instant>
    private lateinit var eventTrackerContext: ApiContext<EventTrackerCall>
    private lateinit var eventTrackerInternal: EventTrackerInstance

    @BeforeTest
    fun setUp() {
        mockEventClient = mock()
        mockTimestampProvider = mock()
        eventTrackerContext = EventTrackerContext(expectedEvents)

        eventTrackerInternal =
            EventTrackerInternal(mockEventClient, eventTrackerContext, mockTimestampProvider)
    }

    @Test
    fun testTrackEvent_shouldMakeCall_onClient() = runTest {
        everySuspend { mockEventClient.registerEvent(event) } returns Unit
        everySuspend { mockTimestampProvider.provide() } returns timestamp

        eventTrackerInternal.trackEvent(customEvent)

        verifySuspend {
            mockTimestampProvider.provide()
            mockEventClient.registerEvent(event)
        }
    }

    @Test
    fun testActivate_should_send_calls_to_client() = runTest {
        everySuspend { mockEventClient.registerEvent(event) } returns Unit
        everySuspend { mockEventClient.registerEvent(event2) } returns Unit

        eventTrackerInternal.activate()

        verifySuspend {
            mockEventClient.registerEvent(event)
            mockEventClient.registerEvent(event2)
        }
    }
}