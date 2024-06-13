package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.core.providers.Provider
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test

class EventTrackerGathererTests  {

    private companion object {
        val customEvent = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
        val timestamp = Clock.System.now()

        val trackEvent = EventTrackerCall.TrackEvent(
            Event(
                EventType.CUSTOM,
                "testEvent",
                mapOf("testAttribute" to "testValue"),
                timestamp.toString()
            )
        )
        val expected: MutableList<EventTrackerCall> = mutableListOf(trackEvent)
    }

    
    private lateinit var mockTimestampProvider: Provider<Instant>
    private lateinit var context: EventTrackerContext
    private val gatherer: EventTrackerGatherer by lazy {
        mockTimestampProvider = mock()

        every { mockTimestampProvider.provide() } returns timestamp
        context = EventTrackerContext(mutableListOf())

        EventTrackerGatherer(context, mockTimestampProvider)
    }


    @Test
    fun testGathering() = runTest {
        gatherer.trackEvent(customEvent)

        context.calls shouldBe expected
    }
}
