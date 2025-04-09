package com.emarsys.api.geofence

import com.emarsys.api.SdkState
import com.emarsys.api.geofence.model.Geofence
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GeofenceTrackerTests {

    private companion object {
        const val SET_ENABLED = true
        val testGeofence = Geofence("testGeofence", 12.3, 34.5, 10.0, null, listOf())
        val testException = Exception()
    }

    private lateinit var mockLoggingGeofenceTracker: GeofenceTrackerInstance
    private lateinit var mockGathererGeofenceTracker: GeofenceTrackerInstance
    private lateinit var mockGeofenceTrackerInternal: GeofenceTrackerInstance
    private lateinit var sdkContext: SdkContextApi

    private lateinit var geofenceTracker: GeofenceTracker<GeofenceTrackerInstance, GeofenceTrackerInstance, GeofenceTrackerInstance>

    private val mainDispatcher = StandardTestDispatcher()

    init {
        Dispatchers.setMain(mainDispatcher)
    }

    @BeforeTest
    fun setup() = runTest {
        mockLoggingGeofenceTracker = mock()
        mockGathererGeofenceTracker = mock()
        mockGeofenceTrackerInternal = mock()

        sdkContext = SdkContext(
            StandardTestDispatcher(),
            mainDispatcher,
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf(),
            logBreadcrumbsQueueSize = 10
        )

        everySuspend { mockLoggingGeofenceTracker.activate() } returns Unit
        everySuspend { mockGathererGeofenceTracker.activate() } returns Unit
        everySuspend { mockGeofenceTrackerInternal.activate() } returns Unit

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
        advanceUntilIdle()

        geofenceTracker.registeredGeofences shouldBe listOf(testGeofence)

        verify { mockGathererGeofenceTracker.registeredGeofences }
    }

    @Test
    fun testRegisteredGeofences_activeState() = runTest {
        every { mockGeofenceTrackerInternal.registeredGeofences } returns listOf(testGeofence)

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        geofenceTracker.registeredGeofences shouldBe listOf(testGeofence)

        verify { mockGeofenceTrackerInternal.registeredGeofences }
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
        advanceUntilIdle()

        geofenceTracker.isEnabled shouldBe true

        verify { mockGathererGeofenceTracker.isEnabled }
    }

    @Test
    fun testIsEnabled_activeState() = runTest {
        every { mockGeofenceTrackerInternal.isEnabled } returns false

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        geofenceTracker.isEnabled shouldBe false

        verify { mockGeofenceTrackerInternal.isEnabled }
    }

    @Test
    fun testEnable_inactiveState() = runTest {
        everySuspend { mockLoggingGeofenceTracker.enable() } returns Unit

        geofenceTracker.enable()

        verifySuspend { mockLoggingGeofenceTracker.enable() }
    }

    @Test
    fun testEnable_onHoldState() = runTest {
        everySuspend { mockGathererGeofenceTracker.enable() } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        geofenceTracker.enable()

        verifySuspend { mockGathererGeofenceTracker.enable() }
    }

    @Test
    fun testEnable_activeState() = runTest {
        everySuspend { mockGeofenceTrackerInternal.enable() } returns Unit

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        geofenceTracker.enable()

        verifySuspend { mockGeofenceTrackerInternal.enable() }
    }

    @Test
    fun testEnable_activeState_throws() = runTest {
        everySuspend { mockGeofenceTrackerInternal.enable() } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = geofenceTracker.enable()

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testDisable_inactiveState() = runTest {
        everySuspend { mockLoggingGeofenceTracker.disable() } returns Unit

        geofenceTracker.disable()

        verifySuspend { mockLoggingGeofenceTracker.disable() }
    }

    @Test
    fun testDisable_onHoldState() = runTest {
        everySuspend { mockGathererGeofenceTracker.disable() } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        geofenceTracker.disable()

        verifySuspend { mockGathererGeofenceTracker.disable() }
    }

    @Test
    fun testDisable_activeState() = runTest {
        everySuspend { mockGeofenceTrackerInternal.disable() } returns Unit

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        geofenceTracker.disable()

        verifySuspend { mockGeofenceTrackerInternal.disable() }
    }

    @Test
    fun testDisable_activeState_throws() = runTest {
        everySuspend { mockGeofenceTrackerInternal.disable() } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = geofenceTracker.disable()

        result.exceptionOrNull() shouldBe testException
    }

    @Test
    fun testSetInitialEnterTriggerEnabled_inactiveState() = runTest {
        everySuspend {
            mockLoggingGeofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED)
        } returns Unit

        geofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED)

        verifySuspend { mockLoggingGeofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED) }
    }

    @Test
    fun testSetInitialEnterTriggerEnabled_onHoldState() = runTest {
        everySuspend { mockGathererGeofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED) } returns Unit

        sdkContext.setSdkState(SdkState.onHold)
        advanceUntilIdle()

        geofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED)

        verifySuspend {
            mockGathererGeofenceTracker.setInitialEnterTriggerEnabled(
                SET_ENABLED
            )
        }
    }

    @Test
    fun testSetInitialEnterTriggerEnabled_activeState() = runTest {
        everySuspend { mockGeofenceTrackerInternal.setInitialEnterTriggerEnabled(SET_ENABLED) } returns Unit

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        geofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED)

        verifySuspend {
            mockGeofenceTrackerInternal.setInitialEnterTriggerEnabled(
                SET_ENABLED
            )
        }
    }

    @Test
    fun testSetInitialEnterTriggerEnabled_activeState_throws() = runTest {
        everySuspend { mockGeofenceTrackerInternal.setInitialEnterTriggerEnabled(SET_ENABLED) } throws testException

        sdkContext.setSdkState(SdkState.active)
        advanceUntilIdle()

        val result = geofenceTracker.setInitialEnterTriggerEnabled(SET_ENABLED)

        result.exceptionOrNull() shouldBe testException
    }
}