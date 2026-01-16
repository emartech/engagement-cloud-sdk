package com.emarsys.api.tracking

import com.emarsys.api.event.model.CustomEvent
import com.emarsys.api.event.model.NavigateEvent
import com.emarsys.api.tracking.model.JsCustomEvent
import com.emarsys.tracking.TrackingApi
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
        jsTracking = JSTracking(mockEventTrackerApi, TestScope())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTrackEvent_shouldCall_trackCustomEvent_onEventTrackerApi_withCorrectParam() =
        runTest {
            val testName = "testName"
            val testAttributesMap = mapOf("testKey" to "testValue")
            everySuspend { mockEventTrackerApi.track(any()) } returns Result.success(
                Unit
            )
            val jsCustomEvent: JsCustomEvent =
                js("{ name: testName, attributes: { 'testKey': 'testValue' } }").unsafeCast<com.emarsys.api.tracking.model.JsCustomEvent>()

            jsTracking.trackEvent(jsCustomEvent).await()

            verifySuspend { mockEventTrackerApi.track(CustomEvent(testName, testAttributesMap)) }
        }

    @Test
    fun testTrackNavigation_shouldCall_trackCustomEvent_onEventTrackerApi_withCorrectParam() =
        runTest {
            val testLocation = "testLocation"
            everySuspend { mockEventTrackerApi.track(any()) } returns Result.success(
                Unit
            )

            jsTracking.trackNavigation(testLocation).await()

            verifySuspend { mockEventTrackerApi.track(NavigateEvent(testLocation)) }
        }

    @Test
    fun testTrackEvent_shouldThrowException_ifPayloadMappingFails() = runTest {
        everySuspend { mockEventTrackerApi.track(any()) } returns Result.success(
            Unit
        )
        val testName = "testName"
        val customEvent: JsCustomEvent =
            js("{ name: testName, attributes: { 'testKey': {} }}").unsafeCast<JsCustomEvent>()

        shouldThrow<IllegalArgumentException> {
            jsTracking.trackEvent(customEvent).await()
        }
    }

    @Test
    fun testTrackEvent_shouldThrowException_ifEventTrackingFails() = runTest {
        val testName = "testName"
        val testAttributesMap = mapOf("testKey" to "testValue")
        val customEvent: JsCustomEvent =
            js("{ name: testName, attributes: { 'testKey': 'testValue' } }").unsafeCast<JsCustomEvent>()

        everySuspend {
            mockEventTrackerApi.track(
                CustomEvent(
                    testName,
                    testAttributesMap
                )
            )
        } returns Result.failure(
            Exception()
        )

        shouldThrow<Exception> {
            jsTracking.trackEvent(customEvent).await()
        }
    }
}