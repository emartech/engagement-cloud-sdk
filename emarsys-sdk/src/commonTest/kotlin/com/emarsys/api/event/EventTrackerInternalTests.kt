package com.emarsys.api.event

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.Logger
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.networking.clients.event.model.SdkEvent
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test

class EventTrackerInternalTests {

    private companion object {
        const val UUID = "testUUID"
        val timestamp = Clock.System.now()
        val customEvent = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
        val event = SdkEvent.External.Custom(
            id = UUID,
            name = "testEvent",
            attributes = buildJsonObject { put("testAttribute", JsonPrimitive("testValue")) },
            timestamp = timestamp,
        )

        val event2 = SdkEvent.Internal.Sdk.AppStart(
            id = UUID,
            attributes = buildJsonObject { put("testAttribute2", JsonPrimitive("testValue2")) },
            timestamp = timestamp
        )

        val trackEvent = EventTrackerCall.TrackEvent(event)
        val trackEvent2 = EventTrackerCall.TrackEvent(event2)
        val expectedEvents: MutableList<EventTrackerCall> = mutableListOf(trackEvent, trackEvent2)
    }

    private lateinit var mockSdkEventDistributor: SdkEventDistributorApi
    private lateinit var mockTimestampProvider: InstantProvider
    private lateinit var mockUuidProvider: UuidProviderApi
    private lateinit var eventTrackerInternal: EventTrackerInstance
    private lateinit var eventTrackerContext: EventTrackerContextApi
    private lateinit var logger: Logger

    @BeforeTest
    fun setUp() {
        mockSdkEventDistributor = mock()
        mockTimestampProvider = mock()
        mockUuidProvider = mock()
        every { mockUuidProvider.provide() } returns UUID
        logger = SdkLogger("TestLoggerName", ConsoleLogger(), sdkContext = mock())

        eventTrackerContext = EventTrackerContext(expectedEvents)

        eventTrackerInternal =
            EventTrackerInternal(
                mockSdkEventDistributor,
                eventTrackerContext,
                mockTimestampProvider,
                mockUuidProvider,
                logger
            )
    }

    @Test
    fun testTrackEvent_shouldMakeCall_onClient() = runTest {
        everySuspend { mockSdkEventDistributor.registerAndStoreEvent(event) } returns Unit
        everySuspend { mockTimestampProvider.provide() } returns timestamp

        eventTrackerInternal.trackEvent(customEvent)

        verifySuspend {
            mockTimestampProvider.provide()
            mockSdkEventDistributor.registerAndStoreEvent(event)
        }
    }

    @Test
    fun testActivate_should_send_calls_to_client() = runTest {
        everySuspend { mockSdkEventDistributor.registerAndStoreEvent(event) } returns Unit
        everySuspend { mockSdkEventDistributor.registerAndStoreEvent(event2) } returns Unit

        eventTrackerInternal.activate()

        verifySuspend {
            mockSdkEventDistributor.registerAndStoreEvent(event)
            mockSdkEventDistributor.registerAndStoreEvent(event2)
        }
    }
}