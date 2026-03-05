package com.sap.ec.api.tracking

import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.api.event.model.NavigateEvent
import com.sap.ec.api.tracking.model.JsCustomEvent
import com.sap.ec.api.tracking.model.JsNavigateEvent
import com.sap.ec.tracking.TrackingApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
        jsTracking = JSTracking(mockEventTrackerApi, sdkLogger = mock(MockMode.autofill))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTrack_shouldCall_track_onEventTrackerApi_withCustomEvent() =
        runTest {
            val testName = "testName"
            val testAttributesMap = mapOf("testKey" to "testValue")
            everySuspend { mockEventTrackerApi.track(any()) } returns Result.success(
                Unit
            )

            val jsCustomEvent: JsCustomEvent =
                js("{ type: 'CUSTOM', name: testName, attributes: { 'testKey': 'testValue' } }").unsafeCast<JsCustomEvent>()

            jsTracking.track(jsCustomEvent)

            verifySuspend { mockEventTrackerApi.track(CustomEvent(testName, testAttributesMap)) }
        }

    @Test
    fun testTrack_shouldCall_track_onEventTrackerApi_withNavigateEvent() =
        runTest {
            everySuspend { mockEventTrackerApi.track(any()) } returns Result.success(
                Unit
            )
            val location = "https://example.com"
            val jsNavigateEvent: JsNavigateEvent =
                js("{ type: 'NAVIGATE', location: location }").unsafeCast<JsNavigateEvent>()

            jsTracking.track(jsNavigateEvent)

            verifySuspend { mockEventTrackerApi.track(NavigateEvent(location)) }
        }

    @Test
    fun testTrackEvent_shouldThrowException_ifEventIsMissingARequiredProperty() =
        runTest {
            everySuspend { mockEventTrackerApi.track(any()) } returns Result.success(
                Unit
            )
            val jsNavigateEvent: JsNavigateEvent =
                js("{ type: 'NAVIGATE', location: undefined }").unsafeCast<JsNavigateEvent>()

            val exception =
                shouldThrow<IllegalArgumentException> { jsTracking.track(jsNavigateEvent) }

            exception.message shouldBe "Failed to parse event."
        }

    @Test
    fun testTrackEvent_shouldThrowException_ifInvalidEventTypeIsPassed() =
        runTest {
            everySuspend { mockEventTrackerApi.track(any()) } returns Result.success(
                Unit
            )
            val jsNavigateEvent: JsNavigateEvent =
                js("{ type: 'TEST', location: 'location' }").unsafeCast<JsNavigateEvent>()

            val exception =
                shouldThrow<IllegalArgumentException> { jsTracking.track(jsNavigateEvent) }

            exception.message shouldBe "Invalid event type: TEST"
        }

    @Test
    fun testTrackEvent_shouldThrowException_ifPayloadMappingFails() = runTest {
        everySuspend { mockEventTrackerApi.track(any()) } returns Result.success(
            Unit
        )
        val testName = "testName"
        val customEvent: JsCustomEvent =
            js("{ type: 'CUSTOM', name: testName, attributes: { 'testKey': {} }}").unsafeCast<JsCustomEvent>()

        val exception = shouldThrow<IllegalArgumentException> {
            jsTracking.track(customEvent)
        }
        exception.message shouldBe "Failed to parse event."
        exception.cause?.message shouldBe "Failed to parse attributes map."
    }

    @Test
    fun testTrackEvent_shouldThrowException_eventTypeIsUnknown() = runTest {
        everySuspend { mockEventTrackerApi.track(any()) } returns Result.success(
            Unit
        )
        val testName = "testName"
        val customEvent: JsCustomEvent =
            js("{ type: 'unknown', name: testName, attributes: { 'testKey': {} }}").unsafeCast<JsCustomEvent>()

        val exception = shouldThrow<IllegalArgumentException> {
            jsTracking.track(customEvent)
        }
        exception.message shouldBe "Invalid event type: unknown"
    }

    @Test
    fun testTrackEvent_shouldThrowException_ifEventTrackingFails() = runTest {
        val testName = "testName"
        val testAttributesMap = mapOf("testKey" to "testValue")
        val customEvent: JsCustomEvent =
            js("{ type: 'CUSTOM', name: testName, attributes: { 'testKey': 'testValue' } }").unsafeCast<JsCustomEvent>()
        val testExceptionMessage = "Tracking failed"
        everySuspend {
            mockEventTrackerApi.track(
                CustomEvent(
                    testName,
                    testAttributesMap
                )
            )
        } returns Result.failure(
            Exception(testExceptionMessage)
        )

        val exception = shouldThrow<Exception> {
            jsTracking.track(customEvent)
        }
        exception.message shouldBe testExceptionMessage
    }
}