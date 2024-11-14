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

class ClientIdProviderTests {
    private companion object {
        const val UUID = "testUUID"
        const val CLIENT_ID = "testHW_ID"
    }

    private lateinit var mockStorage: TypedStorageApi<String?>
    private lateinit var mockUUIDProvider: Provider<String>
    private lateinit var provider: ClientIdProvider

    @BeforeTest
    fun setup() {
        mockStorage = mock()
        mockUUIDProvider = mock()
        every { mockUUIDProvider.provide() } returns UUID
        provider = ClientIdProvider(mockUUIDProvider, mockStorage)
    }

    @Test
    fun testProvide_shouldProvideClientId_from_storage() {
        every { mockStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) } returns CLIENT_ID

        provider.provide() shouldBe CLIENT_ID
    }

    @Test
    fun testProvide_shouldGenerate_andStoreNewId_when_no_clientId_in_storage() {
        every { mockStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) } returns null
        every { mockStorage.put(SdkConstants.CLIENT_ID_STORAGE_KEY, UUID) } returns Unit
        every { mockUUIDProvider.provide() } returns UUID

        provider.provide() shouldBe UUID
        verify { mockStorage.put(SdkConstants.CLIENT_ID_STORAGE_KEY, UUID) }
    }
}