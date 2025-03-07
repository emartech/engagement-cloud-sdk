package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.providers.Provider

import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test

class EventTrackerGathererTests {

    private companion object {
        const val UUID = "testUUID"
        val customEvent = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
        val timestamp = Clock.System.now()

        val trackEvent = EventTrackerCall.TrackEvent(
            SdkEvent.External.Custom(
                id = UUID,
                name = "testEvent",
                attributes = buildJsonObject { put("testAttribute", JsonPrimitive("testValue")) },
                timestamp = timestamp
            )
        )
        val expected: MutableList<EventTrackerCall> = mutableListOf(trackEvent)
    }


    private lateinit var mockTimestampProvider: Provider<Instant>
    private lateinit var mockUuidProvider: Provider<String>
    private lateinit var context: EventTrackerContext
    private val gatherer: EventTrackerGatherer by lazy {
        mockTimestampProvider = mock()
        mockUuidProvider = mock()

        every { mockTimestampProvider.provide() } returns timestamp
        every { mockUuidProvider.provide() } returns UUID
        context = EventTrackerContext(mutableListOf())
        val logger = SdkLogger(ConsoleLogger())

        EventTrackerGatherer(context, mockTimestampProvider, mockUuidProvider, logger)
    }


    @Test
    fun testGathering() = runTest {
        gatherer.trackEvent(customEvent)
        context.calls shouldBe expected
    }
}
