package com.emarsys.api.tracking

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.tracking.TrackingApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.capture.Capture.Companion.slot
import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.SerializationException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSTrackingTests {
    private lateinit var jsTracking: JSTrackingApi
    private lateinit var mockEventTrackerApi: TrackingApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockEventTrackerApi = mock()
        jsTracking = JSTracking(mockEventTrackerApi, TestScope())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTrack_shouldCall_trackCustomEvent_onEventTrackerApi_withCorrectParam() =
        runTest {
            val testName = "testName"
            val payloadValue = "testValue"
            val payloadKey = "testKey"
            val testPayload = js("{}")
            testPayload[payloadKey] = payloadValue
            val eventSlot: SlotCapture<CustomEvent> = slot()
            everySuspend { mockEventTrackerApi.track(capture(eventSlot)) } returns Result.success(
                Unit
            )

            jsTracking.track(testName, testPayload).await()

            eventSlot.get().name shouldBe testName
            eventSlot.get().attributes shouldBe mapOf(payloadKey to payloadValue)
        }

    @Test
    fun testTrack_shouldCall_throwException_ifPayloadMappingFails() = runTest {
        val testName = "testName"
        val payloadValue = js("""{"nested":"object"}""")
        val payloadKey = "testKey"
        val testPayload = js("{}")
        testPayload[payloadKey] = payloadValue

        shouldThrow<SerializationException> {
            jsTracking.track(testName, testPayload).await()
        }
    }

    @Test
    fun testTrack_shouldCall_throwException_ifEventTrackingFails() = runTest {
        val testName = "testName"
        val payloadKey = "testKey"
        val payloadValue = "testValue"
        val testPayload = js("{}")
        testPayload[payloadKey] = payloadValue

        everySuspend {
            mockEventTrackerApi.track(
                CustomEvent(
                    testName,
                    mapOf(payloadKey to payloadValue)
                )
            )
        } returns Result.failure(
            Exception()
        )

        shouldThrow<Exception> {
            jsTracking.track(testName, testPayload).await()
        }
    }
}