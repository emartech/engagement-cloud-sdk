package com.sap.ec.api.event

import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.log.SdkLogger
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.core.storage.StorageApi
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class EventTrackerGathererTests {
    private companion object {
        const val STORE_ID = "testStoreId"
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
    private lateinit var threadSafePersistentStore: ThreadSafePersistentStoreApi<EventTrackerCall>
    private lateinit var mockStorage: StorageApi

    @BeforeTest
    fun setup() {
        mockTimestampProvider = mock()
        mockUuidProvider = mock()
        mockStorage = mock(MockMode.autofill)
        threadSafePersistentStore = ThreadSafePersistentStore(
            STORE_ID,
            mockStorage,
            EventTrackerCall.serializer()
        )

        every { mockTimestampProvider.provide() } returns timestamp
        every { mockUuidProvider.provide() } returns UUID
        val logger = SdkLogger(
            "TestLoggerName",
            mock(MockMode.autofill),
            remoteLogger = null,
            logConfigHolder = mock()
        )

        gatherer = EventTrackerGatherer(
            threadSafePersistentStore,
            mockTimestampProvider,
            mockUuidProvider,
            logger
        )
    }

    @Test
    fun testGathering() = runTest {
        gatherer.trackEvent(customEvent)

        threadSafePersistentStore.items shouldBe expected
    }

    @Test
    fun testGathering_shouldThrowIllegalArgumentException_ifEventName_isBlank() = runTest {
        val testEvent = CustomEvent("   ", mapOf("testAttribute" to "testValue"))

        shouldThrow<IllegalArgumentException> { gatherer.trackEvent(testEvent) }

        threadSafePersistentStore.items shouldBe listOf()
    }
}
