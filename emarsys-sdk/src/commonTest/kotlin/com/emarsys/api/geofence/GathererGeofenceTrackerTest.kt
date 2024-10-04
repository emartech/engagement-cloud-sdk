package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererGeofenceTrackerTest {

    private lateinit var gathererGeofenceTracker: GathererGeofenceTracker
    private lateinit var geofenceContext: GeofenceContext

    @BeforeTest
    fun setup() {
        geofenceContext = GeofenceTrackerContext(mutableListOf())

        gathererGeofenceTracker = GathererGeofenceTracker(geofenceContext)
    }

    @Test
    fun testRegisteredGeofences() = runTest {
        val testGeofence = Geofence("testGeofence", 12.3, 34.5, 10.0, null, listOf())
        geofenceContext.registeredGeofences.add(testGeofence)

        val result = gathererGeofenceTracker.registeredGeofences

        result shouldBe listOf(testGeofence)
    }

    @Test
    fun testIsEnabled() = runTest {
        geofenceContext.isGeofenceTrackerEnabled = true

        gathererGeofenceTracker.isEnabled shouldBe true
    }

    @Test
    fun testEnable() = runTest {
        val testCall = GeofenceTrackerCall.Enable()

        gathererGeofenceTracker.enable()

        geofenceContext.calls.contains(testCall) shouldBe true
    }

    @Test
    fun testDisable() = runTest {
        val testCall = GeofenceTrackerCall.Disable()

        gathererGeofenceTracker.disable()

        geofenceContext.calls.contains(testCall) shouldBe true
    }

    @Test
    fun testSetInitialEnterTriggerEnabled() = runTest {
        val testCall = GeofenceTrackerCall.SetInitialEnterTriggerEnabled(true)

        gathererGeofenceTracker.setInitialEnterTriggerEnabled(true)

        geofenceContext.calls.contains(testCall) shouldBe true
    }
}