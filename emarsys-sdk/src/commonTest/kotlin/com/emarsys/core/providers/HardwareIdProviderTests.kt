package com.emarsys.core.providers

import com.emarsys.SdkConstants
import com.emarsys.core.storage.TypedStorageApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.matchers.shouldBe
import kotlin.test.BeforeTest
import kotlin.test.Test

class HardwareIdProviderTests {
    private companion object {
        const val UUID = "testUUID"
        const val HW_ID = "testHW_ID"
    }

    private lateinit var mockStorage: TypedStorageApi<String?>
    private lateinit var mockUUIDProvider: Provider<String>
    private lateinit var provider: HardwareIdProvider

    @BeforeTest
    fun setup() {
        mockStorage = mock()
        mockUUIDProvider = mock()
        every { mockUUIDProvider.provide() } returns UUID
        provider = HardwareIdProvider(mockUUIDProvider, mockStorage)
    }

    @Test
    fun testProvide_shouldProvideHwId_from_storage() {
        every { mockStorage.get(SdkConstants.HARDWARE_ID_STORAGE_KEY) } returns HW_ID

        provider.provide() shouldBe HW_ID
    }

    @Test
    fun testProvide_shouldGenerate_andStoreNewId_when_no_hwid_in_storage() {
        every { mockStorage.get(SdkConstants.HARDWARE_ID_STORAGE_KEY) } returns null
        every { mockStorage.put(SdkConstants.HARDWARE_ID_STORAGE_KEY, UUID) } returns Unit
        every { mockUUIDProvider.provide() } returns UUID

        provider.provide() shouldBe UUID
        verify { mockStorage.put(SdkConstants.HARDWARE_ID_STORAGE_KEY, UUID) }
    }
}