package com.sap.ec.disable.states

import com.sap.ec.core.device.DeviceInfoStorageApi
import dev.mokkery.MockMode
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClearDeviceInfoStateTests {
    private lateinit var mockDeviceInfoUpdater: DeviceInfoStorageApi
    private lateinit var clearDeviceInfoState: ClearDeviceInfoState

    @BeforeTest
    fun setup() {
        mockDeviceInfoUpdater = mock(MockMode.autofill)
        clearDeviceInfoState = ClearDeviceInfoState(mockDeviceInfoUpdater)
    }

    @Test
    fun active_shouldCall_clearDeviceInfo_andReturnSuccess() = runTest {
        val result = clearDeviceInfoState.active()

        verify { mockDeviceInfoUpdater.clear() }
        result shouldBe Result.success(Unit)
    }

    @Test
    fun active_shouldCall_clearDeviceInfo_andReturnFailure_withTheError() = runTest {
        val testError = RuntimeException("Operation failed")
        everySuspend { mockDeviceInfoUpdater.clear() } throws testError

        val result = clearDeviceInfoState.active()

        verify { mockDeviceInfoUpdater.clear() }
        result shouldBe Result.failure(testError)
    }
}