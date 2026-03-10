package com.sap.ec.api.event

import com.sap.ec.api.SdkState
import com.sap.ec.api.event.model.CustomEvent
import com.sap.ec.context.SdkContextApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventTrackerTests {
    private companion object {
        val event = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
    }

    private lateinit var mockLoggingEventTracker: EventTrackerInstance
    private lateinit var mockEventTrackerGatherer: EventTrackerInstance
    private lateinit var mockEventTrackerInternal: EventTrackerInstance
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var eventTracker: EventTracker<EventTrackerInstance, EventTrackerInstance, EventTrackerInstance>

    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(mainDispatcher)
        mockLoggingEventTracker = mock(MockMode.autofill)
        mockEventTrackerGatherer = mock(MockMode.autofill)
        mockEventTrackerInternal = mock(MockMode.autofill)

        mockSdkContext = mock(MockMode.autofill)
        every { mockSdkContext.sdkDispatcher } returns mainDispatcher

        eventTracker =
            EventTracker(
                mockLoggingEventTracker,
                mockEventTrackerGatherer,
                mockEventTrackerInternal,
                mockSdkContext
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testTrackEvent_inactiveState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Initialized)
        eventTracker.registerOnContext()

        eventTracker.trackEvent(event)

        verifySuspend { mockLoggingEventTracker.trackEvent(event) }
    }

    @Test
    fun testTrackEvent_onHoldState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.OnHold)
        eventTracker.registerOnContext()

        eventTracker.trackEvent(event)

        verifySuspend { mockEventTrackerGatherer.trackEvent(event) }
    }

    @Test
    fun testTrackEvent_activeState() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        eventTracker.registerOnContext()

        eventTracker.trackEvent(event)

        verifySuspend { mockEventTrackerInternal.trackEvent(event) }
    }

    @Test
    fun testTrackEvent_activeState_shouldReturnErrorInResult() = runTest {
        every { mockSdkContext.currentSdkState } returns MutableStateFlow(SdkState.Active)
        val expectException = Exception()
        everySuspend { mockEventTrackerInternal.trackEvent(event) } throws expectException
        eventTracker.registerOnContext()

        val result = eventTracker.trackEvent(event)

        result.exceptionOrNull() shouldBe expectException
    }
}