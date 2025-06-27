package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
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

    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockUuidProvider: UuidProviderApi
    private lateinit var gatherer: EventTrackerGatherer
    private lateinit var eventTrackerContext: EventTrackerContextApi

    @BeforeTest
    fun setup() {
        mockTimestampProvider = mock()
        mockUuidProvider = mock()

        every { mockTimestampProvider.provide() } returns timestamp
        every { mockUuidProvider.provide() } returns UUID
        val logger = SdkLogger("TestLoggerName", mock(MockMode.autofill), remoteLogger = null, sdkContext = mock())

        eventTrackerContext = EventTrackerContext(mutableListOf())

        gatherer = EventTrackerGatherer(
            eventTrackerContext,
            mockTimestampProvider,
            mockUuidProvider,
            logger
        )
    }

    @AfterTest
    fun teardown() {
        eventTrackerContext.calls.clear()

    }

    @Test
    fun testGathering() = runTest {
        gatherer.trackEvent(customEvent)

        eventTrackerContext.calls shouldBe expected
    }
}
