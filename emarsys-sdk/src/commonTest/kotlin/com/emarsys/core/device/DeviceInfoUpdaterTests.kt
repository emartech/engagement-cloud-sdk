package com.emarsys.core.device

import com.emarsys.core.device.DeviceConstants.DEVICE_INFO_STORAGE_KEY
import com.emarsys.core.storage.StringStorageApi
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
        val testDeviceInfoHash = TEST_DEVICE_INFO.hashCode().toString()
    }

    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var deviceInfoUpdater: DeviceInfoUpdaterApi

    @BeforeTest
    fun setup() {
        mockStringStorage = mock(MockMode.autofill)
        deviceInfoUpdater = DeviceInfoUpdater(mockStringStorage)
    }

    @Test
    fun updateDeviceInfoHash_shouldStore_theHash_ofTheProvidedInput() {

        deviceInfoUpdater.updateDeviceInfoHash(TEST_DEVICE_INFO)

        verify { mockStringStorage.put(DEVICE_INFO_STORAGE_KEY, testDeviceInfoHash) }
    }

    @Test
    fun hasDeviceInfoChanged_shouldReturnTrue_ifActualDeviceInfoHash_isDifferent_fromTheStored() =
        runTest {
            val storedHash = "storedHash"
            everySuspend { mockStringStorage.get(DEVICE_INFO_STORAGE_KEY) } returns storedHash

            val hasChanged = deviceInfoUpdater.hasDeviceInfoChanged(TEST_DEVICE_INFO)

            verify { mockStringStorage.get(DEVICE_INFO_STORAGE_KEY) }

            hasChanged shouldBe true
        }

    @Test
    fun hasDeviceInfoChanged_shouldReturnFalse_ifActualDeviceInfoHash_isTheSame_asTheStored() =
        runTest {
            everySuspend { mockStringStorage.get(DEVICE_INFO_STORAGE_KEY) } returns testDeviceInfoHash

            val hasChanged = deviceInfoUpdater.hasDeviceInfoChanged(TEST_DEVICE_INFO)

            verify { mockStringStorage.get(DEVICE_INFO_STORAGE_KEY) }

            hasChanged shouldBe false
        }
}