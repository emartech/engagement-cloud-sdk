package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererGeofenceTrackerTest {

    private lateinit var gathererGeofenceTracker: GathererGeofenceTracker
    private lateinit var geofenceTrackerContext: GeofenceContextApi

    @BeforeTest
    fun setup() {
        geofenceTrackerContext = GeofenceTrackerContext(mutableListOf())
        gathererGeofenceTracker =
            GathererGeofenceTracker(geofenceTrackerContext, GeofenceTrackerConfig)
    }

    @Test
    fun testRegisteredGeofences() = runTest {
        val testGeofence = Geofence("testGeofence", 12.3, 34.5, 10.0, null, listOf())
        GeofenceTrackerConfig.registeredGeofences.add(testGeofence)

        val result = gathererGeofenceTracker.registeredGeofences

        result shouldBe listOf(testGeofence)
    }

    @Test
    fun testIsEnabled() = runTest {
        GeofenceTrackerConfig.isGeofenceTrackerEnabled = true

        gathererGeofenceTracker.isEnabled shouldBe true
    }

    @Test
    fun testEnable() = runTest {
        val testCall = GeofenceTrackerCall.Enable()

        gathererGeofenceTracker.enable()

        geofenceTrackerContext.calls.contains(testCall) shouldBe true
    }

    @Test
    fun testDisable() = runTest {
        val testCall = GeofenceTrackerCall.Disable()

        gathererGeofenceTracker.disable()

        geofenceTrackerContext.calls.contains(testCall) shouldBe true
    }

    @Test
    fun testSetInitialEnterTriggerEnabled() = runTest {
        val testCall = GeofenceTrackerCall.SetInitialEnterTriggerEnabled(true)

        gathererGeofenceTracker.setInitialEnterTriggerEnabled(true)

        geofenceTrackerContext.calls.contains(testCall) shouldBe true
    }
}