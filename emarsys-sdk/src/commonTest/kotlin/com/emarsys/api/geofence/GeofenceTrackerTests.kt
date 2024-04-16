package com.emarsys.api.geofence

import com.emarsys.api.AppEvent
import com.emarsys.api.SdkState
import com.emarsys.api.geofence.model.Geofence
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
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
class GeofenceTrackerTests : TestsWithMocks() {

    override fun setUpMocks() = injectMocks(mocker)

    private companion object {
        const val SET_ENABLED = true
        val testGeofence = Geofence("testGeofence", 12.3, 34.5, 10.0, null, listOf())
        val testEvents = flowOf(AppEvent("testGeofence", mapOf("key" to "value")))
        val testException = Exception()
    }

    @Mock
    lateinit var mockLoggingGeofenceTracker: GeofenceTrackerInstance

    @Mock
    lateinit var mockGathererGeofenceTracker: GeofenceTrackerInstance

    @Mock
    lateinit var mockGeofenceTrackerInternal: GeofenceTrackerInstance

    private lateinit var sdkContext: SdkContextApi

    private lateinit var geofenceTracker: GeofenceTracker<GeofenceTrackerInstance, GeofenceTrackerInstance, GeofenceTrackerInstance>

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

        sdkContext.registeredGeofences.add(testGeofence)

        everySuspending { mockLoggingGeofenceTracker.activate() } returns Unit
        everySuspending { mockGathererGeofenceTracker.activate() } returns Unit
        everySuspending { mockGeofenceTrackerInternal.activate() } returns Unit

        geofenceTracker =
            GeofenceTracker(
                mockLoggingGeofenceTracker,
                mockGathererGeofenceTracker,
                mockGeofenceTrackerInternal,
                sdkContext
            )
        geofenceTracker.registerOnContext()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        mocker.reset()
    }

    @Test
    fun testRegisteredGeofences_inactiveState() = runTest {
        every { mockLoggingGeofenceTracker.registeredGeofences } returns listOf()

        geofenceTracker.registeredGeofences

        verify { mockLoggingGeofenceTracker.registeredGeofences }
    }

    @Test
    fun testRegisteredGeofences_onHoldState() = runTest {
        every { mockGathererGeofenceTracker.registeredGeofences } returns listOf(testGeofence)

        sdkContext.setSdkState(SdkState.onHold)

        geofenceTracker.registeredGeofences shouldBe listOf(testGeofence)

        verify(exhaustive = false) { mockGathererGeofenceTracker.registeredGeofences }
    }

    @Test
    fun testRegisteredGeofences_activeState() = runTest {
        every { mockGeofenceTrackerInternal.registeredGeofences } returns listOf(testGeofence)

        sdkContext.setSdkState(SdkState.active)

        geofenceTracker.registeredGeofences shouldBe listOf(testGeofence)

        verify(exhaustive = false) { mockGeofenceTrackerInternal.registeredGeofences }
    }

    @Test
    fun testEvents_inactiveState() = runTest {
        every { mockLoggingGeofenceTracker.events } returns emptyFlow()

        geofenceTracker.events

        verify { mockLoggingGeofenceTracker.events }
    }

    @Test
    fun testEvents_onHoldState() = runTest {
        every { mockGathererGeofenceTracker.events } returns emptyFlow()

        sdkContext.setSdkState(SdkState.onHold)

        geofenceTracker.events shouldBe emptyFlow()

        verify(exhaustive = false) { mockGathererGeofenceTracker.events }
    }

    @Test
    fun testEvents_activeState() = runTest {
        every { mockGeofenceTrackerInternal.events } returns testEvents

        sdkContext.setSdkState(SdkState.active)

        geofenceTracker.events shouldBe testEvents

        verify(exhaustive = false) { mockGeofenceTrackerInternal.events }
    }

    @Test
    fun testIsEnabled_inactiveState() = runTest {
        every { mockLoggingGeofenceTracker.isEnabled } returns false

        geofenceTracker.isEnabled

        verify { mockLoggingGeofenceTracker.isEnabled }
    }

    @Test
    fun testIsEnabled_onHoldState() = runTest {
        every { mockGathererGeofenceTracker.isEnabled } returns true

        sdkContext.setSdkState(SdkState.onHold)

        geofenceTracker.isEnabled shouldBe true

        verify(exhaustive = false) { mockGathererGeofenceTracker.isEnabled }
    }

    @Test
    fun testIsEnabled_activeState() = runTest {
        every { mockGeofenceTrackerInternal.isEnabled } returns false

        sdkContext.setSdkState(SdkState.active)

        geofenceTracker.isEnabled shouldBe false

        verify(exhaustive = false) { mockGeofenceTrackerInternal.isEnabled }
    }

    @Test
    fun testEnable_inactiveState() = runTest {
        everySuspending { mockLoggingGeofenceTracker.enable() } returns Unit

        geofenceTracker.enable()

        verifyWithSuspend { mockLoggingGeofenceTracker.enable() }
    }

    @Test
    fun testEnable_onHoldState() = runTest {
        everySuspending { mockGathererGeofenceTracker.enable() } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        geofenceTracker.enable()

        verifyWithSuspend(exhaustive = false) { mockGathererGeofenceTracker.enable() }
    }

    @Test
    fun testEnable_activeState() = runTest {
        everySuspending { mockGeofenceTrackerInternal.enable() } returns Unit

        sdkContext.setSdkState(SdkState.active)

        geofenceTracker.enable()

        verifyWithSuspend(exhaustive = false) { mockGeofenceTrackerInternal.enable() }
    }

    @Test
    fun testEnable_activeState_throws() = runTest {
        everySuspending { mockGeofenceTrackerInternal.enable() } runs {
            throw testException
        }

        sdkContext.setSdkState(SdkState.active)

        val result = geofenceTracker.enable()

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testDisable_inactiveState() = runTest {
        everySuspending { mockLoggingGeofenceTracker.disable() } returns Unit

        geofenceTracker.disable()

        verifyWithSuspend { mockLoggingGeofenceTracker.disable() }
    }

    @Test
    fun testDisable_onHoldState() = runTest {
        everySuspending { mockGathererGeofenceTracker.disable() } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        geofenceTracker.disable()

        verifyWithSuspend(exhaustive = false) { mockGathererGeofenceTracker.disable() }
    }

    @Test
    fun testDisable_activeState() = runTest {
        everySuspending { mockGeofenceTrackerInternal.disable() } returns Unit

        sdkContext.setSdkState(SdkState.active)

        geofenceTracker.disable()

        verifyWithSuspend(exhaustive = false) { mockGeofenceTrackerInternal.disable() }
    }

    @Test
    fun testDisable_activeState_throws() = runTest {
        everySuspending { mockGeofenceTrackerInternal.disable() } runs {
            throw testException
        }

        sdkContext.setSdkState(SdkState.active)

        val result = geofenceTracker.disable()

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testSetInitialEnterTriggerEnabled_inactiveState() = runTest {
        everySuspending {
            mockLoggingGeofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED)
        } returns Unit

        geofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED)

        verifyWithSuspend { mockLoggingGeofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED) }
    }

    @Test
    fun testSetInitialEnterTriggerEnabled_onHoldState() = runTest {
        everySuspending { mockGathererGeofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)

        geofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED)

        verifyWithSuspend(exhaustive = false) {
            mockGathererGeofenceTracker.setInitialEnterTriggerEnabled(
                SET_ENABLED
            )
        }
    }

    @Test
    fun testSetInitialEnterTriggerEnabled_activeState() = runTest {
        everySuspending { mockGeofenceTrackerInternal.setInitialEnterTriggerEnabled(SET_ENABLED) } returns Unit

        sdkContext.setSdkState(SdkState.active)

        geofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED)

        verifyWithSuspend(exhaustive = false) {
            mockGeofenceTrackerInternal.setInitialEnterTriggerEnabled(
                SET_ENABLED
            )
        }
    }

    @Test
    fun testSetInitialEnterTriggerEnabled_activeState_throws() = runTest {
        everySuspending { mockGeofenceTrackerInternal.setInitialEnterTriggerEnabled(SET_ENABLED) } runs {
            throw testException
        }

        sdkContext.setSdkState(SdkState.active)

        val result = geofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED)

        result.exceptionOrNull() shouldBe testException
    }
}