package com.emarsys.api.geofence

import com.emarsys.api.geofence.model.Geofence
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JSGeofenceTests {
    private companion object {
        val testGeofences = listOf(
            Geofence(
                id = "id",
                lat = 1.234,
                lon = 2.345,
                radius = 9.876,
                waitInterval = 1.0,
                emptyList()
            )
        )
        const val TEST_ENABLED = false
        val testResultFailure = Result.failure<Unit>(Exception())
    }

    private lateinit var jsGeofence: JSGeofenceApi
    private lateinit var mockGeofenceTrackerApi: GeofenceTrackerApi

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockGeofenceTrackerApi = mock()
        every { mockGeofenceTrackerApi.registeredGeofences } returns testGeofences
        every { mockGeofenceTrackerApi.isEnabled } returns TEST_ENABLED
        jsGeofence = JSGeofence(mockGeofenceTrackerApi, TestScope())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registeredGeofences_shouldCall_registeredGeofences_onGeofenceApi() = runTest {
        jsGeofence.registeredGeofences shouldBe testGeofences
    }

    @Test
    fun isEnabled_shouldCall_isEnabled_onGeofenceApi() = runTest {
        jsGeofence.isEnabled shouldBe false
    }

    @Test
    fun enable_shouldCall_enable_onGeofenceApi() = runTest {
        everySuspend { mockGeofenceTrackerApi.enable() } returns Result.success(Unit)

        jsGeofence.enable().await()

        verifySuspend { mockGeofenceTrackerApi.enable() }
    }

    @Test
    fun enable_shouldThrowException_ifCallFailsOnApi() = runTest {
        everySuspend { mockGeofenceTrackerApi.enable() } returns testResultFailure

        shouldThrow<Exception> { jsGeofence.enable().await() }
    }

    @Test
    fun disable_shouldCall_disable_onGeofenceApi() = runTest {
        everySuspend { mockGeofenceTrackerApi.disable() } returns Result.success(Unit)

        jsGeofence.disable().await()

        verifySuspend { mockGeofenceTrackerApi.disable() }
    }

    @Test
    fun disable_shouldThrowException_ifCallFailsOnApi() = runTest {
        everySuspend { mockGeofenceTrackerApi.disable() } returns testResultFailure

        shouldThrow<Exception> { jsGeofence.disable().await() }
    }

    @Test
    fun setInitialEnterTriggerEnabled_shouldCall_setInitialEnterTriggerEnabled_onGeofenceApi() = runTest {
        everySuspend { mockGeofenceTrackerApi.setInitialEnterTriggerEnabled(true) } returns Result.success(Unit)

        jsGeofence.setInitialEnterTriggerEnabled(true).await()

        verifySuspend { mockGeofenceTrackerApi.setInitialEnterTriggerEnabled(true) }
    }

    @Test
    fun setInitialEnterTriggerEnabled_shouldThrowException_ifCallFailsOnApi() = runTest {
        everySuspend { mockGeofenceTrackerApi.setInitialEnterTriggerEnabled(true) } returns testResultFailure

        shouldThrow<Exception> { jsGeofence.setInitialEnterTriggerEnabled(true).await() }
    }
}