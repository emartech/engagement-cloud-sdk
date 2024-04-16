package com.emarsys.api.geofence

import com.emarsys.api.generic.ApiContext
import com.emarsys.api.geofence.model.Geofence
import com.emarsys.context.DefaultUrls
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.LogLevel
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererGeofenceTrackerTest {

    private lateinit var gathererGeofenceTracker: GathererGeofenceTracker

    private lateinit var geofenceContext: ApiContext<GeofenceTrackerCall>
    private lateinit var sdkContext: SdkContextApi

    @BeforeTest
    fun setup() {
        geofenceContext = GeofenceTrackerContext(mutableListOf())
        sdkContext = SdkContext(
            StandardTestDispatcher(),
            DefaultUrls("", "", "", "", "", "", ""),
            LogLevel.Error,
            mutableSetOf()
        )

        gathererGeofenceTracker = GathererGeofenceTracker(geofenceContext, sdkContext, emptyFlow())
    }

    @Test
    fun testRegisteredGeofences() = runTest {
        val testGeofence = Geofence("testGeofence", 12.3, 34.5, 10.0, null, listOf())
        sdkContext.registeredGeofences.add(testGeofence)

        val result = gathererGeofenceTracker.registeredGeofences

        result shouldBe listOf(testGeofence)
    }

    @Test
    fun testIsEnabled() = runTest {
        sdkContext.isGeofenceTrackerEnabled = true

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