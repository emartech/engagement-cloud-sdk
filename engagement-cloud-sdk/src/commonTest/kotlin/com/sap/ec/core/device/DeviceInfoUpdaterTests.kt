package com.sap.ec.core.device

import com.sap.ec.core.device.DeviceConstants.DEVICE_INFO_STORAGE_KEY
import com.sap.ec.core.storage.StringStorageApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeviceInfoUpdaterTests {
    private companion object {
        const val TEST_DEVICE_INFO = "testDeviceInfo"
    }

    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var deviceInfoUpdater: DeviceInfoUpdaterApi

    @BeforeTest
    fun setup() {
        mockStringStorage = mock(MockMode.autofill)
        deviceInfoUpdater = DeviceInfoUpdater(mockStringStorage)
    }

    @Test
    fun storeDeviceInfo_shouldStoreTheInputString() {
        deviceInfoUpdater.storeDeviceInfo(TEST_DEVICE_INFO)

        verify { mockStringStorage.put(DEVICE_INFO_STORAGE_KEY, TEST_DEVICE_INFO) }
    }

    @Test
    fun hasDeviceInfoChanged_shouldReturnTrue_ifActualDeviceInfo_isDifferent_fromTheStored() =
        runTest {
            val storedDeviceInfo = "storedDeviceInfo"
            everySuspend { mockStringStorage.get(DEVICE_INFO_STORAGE_KEY) } returns storedDeviceInfo

            val hasChanged = deviceInfoUpdater.hasDeviceInfoChanged(TEST_DEVICE_INFO)

            verify { mockStringStorage.get(DEVICE_INFO_STORAGE_KEY) }
            hasChanged shouldBe true
        }

    @Test
    fun hasDeviceInfoChanged_shouldReturnFalse_ifActualDeviceInfo_isTheSame_asTheStored() =
        runTest {
            everySuspend { mockStringStorage.get(DEVICE_INFO_STORAGE_KEY) } returns TEST_DEVICE_INFO

            val hasChanged = deviceInfoUpdater.hasDeviceInfoChanged(TEST_DEVICE_INFO)

            verify { mockStringStorage.get(DEVICE_INFO_STORAGE_KEY) }
            hasChanged shouldBe false
        }
}