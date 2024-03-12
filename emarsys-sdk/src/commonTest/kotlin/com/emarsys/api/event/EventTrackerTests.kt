package com.emarsys.api.event

import com.emarsys.api.SdkResult
import com.emarsys.api.SdkState
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventTrackerTests : TestsWithMocks() {
    override fun setUpMocks() = injectMocks(mocker)

    private companion object {
        val event = CustomEvent("testEvent", mapOf("testAttribute" to "testValue"))
    }

    @Mock
    lateinit var mockLoggingEventTracker: EventTrackerInstance

    @Mock
    lateinit var mockEventTrackerGatherer: EventTrackerInstance

    @Mock
    lateinit var mockEventTrackerInternal: EventTrackerInstance

    private lateinit var sdkContext: SdkContextApi

    private lateinit var eventTracker: EventTracker<EventTrackerInstance, EventTrackerInstance, EventTrackerInstance>

    init {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @BeforeTest
    fun setup() = runTest {
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        everySuspending { mockLoggingEventTracker.activate() } returns Unit
        everySuspending { mockEventTrackerGatherer.activate() } returns Unit
        everySuspending { mockEventTrackerInternal.activate() } returns Unit
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    @Test
    fun testTrackEvent_inactiveState() = runTest {
        everySuspending {
            mockLoggingEventTracker.trackEvent(event)
        } returns SdkResult.Success(Unit)

        eventTracker =
            EventTracker(mockLoggingEventTracker, mockEventTrackerGatherer, mockEventTrackerInternal, sdkContext)

        eventTracker.trackEvent(event)

        verifyWithSuspend(exhaustive = false) {
            mockLoggingEventTracker.trackEvent(event)
        }
    }

    @Test
    fun testTrackEvent_onHoldState() = runTest {
        everySuspending {
            mockEventTrackerGatherer.trackEvent(event)
        } returns SdkResult.Success(Unit)

        eventTracker =
            EventTracker(mockLoggingEventTracker, mockEventTrackerGatherer, mockEventTrackerInternal, sdkContext)

        sdkContext.setSdkState(SdkState.onHold)
        eventTracker.trackEvent(event)

        verifyWithSuspend(exhaustive = false) {
            mockEventTrackerGatherer.trackEvent(event)
        }
    }

    @Test
    fun testTrackEvent_activeState() = runTest {
        everySuspending {
            mockEventTrackerInternal.trackEvent(event)
        } returns SdkResult.Success(Unit)

        eventTracker =
            EventTracker(mockLoggingEventTracker, mockEventTrackerGatherer, mockEventTrackerInternal, sdkContext)

        sdkContext.setSdkState(SdkState.active)
        eventTracker.trackEvent(event)

        verifyWithSuspend(exhaustive = false) {
            mockEventTrackerInternal.trackEvent(event)
        }
    }
}