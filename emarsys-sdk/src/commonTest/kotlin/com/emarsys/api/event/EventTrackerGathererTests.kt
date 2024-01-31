package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.event.model.EventType
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class EventTrackerGathererTests {
    private companion object {
        val customEvent = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))

        val trackEvent = EventTrackerCall.TrackEvent(Event(EventType.CUSTOM, "testEvent", mapOf("testAttribute" to "testValue")))
        val expected: MutableList<EventTrackerCall> = mutableListOf(trackEvent)
    }

    private lateinit var context: EventTrackerContext
    private lateinit var gatherer: EventTrackerGatherer

    @BeforeTest
    fun setup() {
        context = EventTrackerContext(expected)
        gatherer = EventTrackerGatherer(context)
    }

    @Test
    fun testGathering() = runTest {
        gatherer.trackEvent(customEvent)

        context.calls shouldBe expected
    }
}
