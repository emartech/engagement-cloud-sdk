package com.emarsys.core.providers

import com.emarsys.SdkConstants
import com.emarsys.core.storage.StringStorageApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClientIdProviderTests {
    private companion object {
        const val UUID = "testUUID"
        const val CLIENT_ID = "testHW_ID"
    }

    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockUUIDProvider: Provider<String>
    private lateinit var provider: ClientIdProvider

    @BeforeTest
    fun setup() {
        mockStringStorage = mock()
        mockUUIDProvider = mock()
        every { mockUUIDProvider.provide() } returns UUID
        provider = ClientIdProvider(mockUUIDProvider, mockStringStorage)
    }

    @Test
    fun testProvide_shouldProvideClientId_from_storage() = runTest {
        everySuspend { mockStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) } returns CLIENT_ID

        provider.provide() shouldBe CLIENT_ID
    }

    @Test
    fun testProvide_shouldGenerate_andStoreNewId_when_no_clientId_in_storage() = runTest {
        everySuspend { mockStringStorage.get(SdkConstants.CLIENT_ID_STORAGE_KEY) } returns null
        everySuspend { mockStringStorage.put(SdkConstants.CLIENT_ID_STORAGE_KEY, UUID) } returns Unit
        everySuspend { mockUUIDProvider.provide() } returns UUID

        provider.provide() shouldBe UUID
        verifySuspend { mockStringStorage.put(SdkConstants.CLIENT_ID_STORAGE_KEY, UUID) }
    }
}