package com.sap.ec.api.event

import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventWaiterApi
import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.log.SdkLogger
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.core.storage.StorageApi
import com.sap.ec.event.SdkEvent
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class EventTrackerInternalTests {

    private companion object {
        const val STORE_ID = "testStoreId"
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
    private lateinit var threadSafePersistentStore: ThreadSafePersistentStoreApi<EventTrackerCall>
    private lateinit var mockStorage: StorageApi
    private lateinit var logger: Logger
    private lateinit var mockWaiter: SdkEventWaiterApi

    @BeforeTest
    fun setUp() {
        mockSdkEventDistributor = mock()
        mockStorage = mock(MockMode.autofill)
        mockTimestampProvider = mock()
        mockUuidProvider = mock()
        mockWaiter = mock()
        everySuspend { mockWaiter.await<Any>() } returns SdkEvent.Internal.Sdk.Answer.Response(
            "0",
            Result.success(Any())
        )
        every { mockUuidProvider.provide() } returns UUID
        everySuspend { mockTimestampProvider.provide() } returns timestamp
        logger = SdkLogger("TestLoggerName", mock(MockMode.autofill), logConfigHolder = mock())
        threadSafePersistentStore = createSafeStore()
        eventTrackerInternal = createEventInternal()
    }


    @Test
    fun testTrackEvent_shouldMakeCall_onClient() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(event) } returns mockWaiter

        eventTrackerInternal.trackEvent(customEvent)

        verifySuspend {
            mockTimestampProvider.provide()
            mockSdkEventDistributor.registerEvent(event)
        }
    }

    @Test
    fun testTrackEvent_shouldThrowIllegalArgumentException_ifEventName_isBlank() = runTest {
        val testEvent = CustomEvent("   ", mapOf("testAttribute" to "testValue"))

        shouldThrow<IllegalArgumentException> { eventTrackerInternal.trackEvent(testEvent) }
    }

    @Test
    fun testActivate_should_send_calls_to_client() = runTest {
        everySuspend { mockSdkEventDistributor.registerEvent(event) } returns mockWaiter
        everySuspend { mockSdkEventDistributor.registerEvent(event2) } returns mockWaiter
        every { mockStorage.get(STORE_ID, any<KSerializer<List<Any>>>()) } returns expectedEvents

        val store = createSafeStore()
        val testInternal = createEventInternal(store)

        testInternal.activate()

        verifySuspend {
            mockSdkEventDistributor.registerEvent(event)
            mockSdkEventDistributor.registerEvent(event2)
        }
    }

    private fun createSafeStore(storage: StorageApi = mockStorage): ThreadSafePersistentStoreApi<EventTrackerCall> {
        return ThreadSafePersistentStore(
            STORE_ID,
            storage,
            EventTrackerCall.serializer()
        )
    }

    private fun createEventInternal(safeStore: ThreadSafePersistentStoreApi<EventTrackerCall> = threadSafePersistentStore): EventTrackerInternal =
        EventTrackerInternal(
            mockSdkEventDistributor,
            safeStore,
            mockTimestampProvider,
            mockUuidProvider,
            logger
        )
}