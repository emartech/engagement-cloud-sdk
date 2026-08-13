package com.sap.ec.api.push

import com.sap.ec.TestEngagementCloudSDKConfig
import com.sap.ec.api.push.PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.storage.StorageApi
import com.sap.ec.core.storage.StringStorageApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PushGathererTests {
    private companion object {
        const val STORE_ID = "storeId"
        const val PUSH_TOKEN = "testPushToken"
        const val APPLICATION_CODE = "testAppCode"
        val registerPushToken = PushCall.RegisterPushToken(PUSH_TOKEN)
        val clearPushToken = PushCall.ClearPushToken(applicationCode = APPLICATION_CODE)

        val expected = mutableListOf(
            registerPushToken,
            clearPushToken
        )
    }

    private lateinit var threadSafePersistentStore: ThreadSafePersistentStoreApi<PushCall>
    private lateinit var mockStorage: StorageApi
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var pushGatherer: PushInstance

    @BeforeTest
    fun setUp() {
        mockStorage = mock(MockMode.autofill)
        threadSafePersistentStore =
            ThreadSafePersistentStore(STORE_ID, mockStorage, PushCall.serializer())
        mockStringStorage = mock()
        mockSdkContext = mock()
        everySuspend { mockSdkContext.getSdkConfig() } returns TestEngagementCloudSDKConfig(
            APPLICATION_CODE
        )
        pushGatherer = PushGatherer(threadSafePersistentStore, mockStringStorage, mockSdkContext)
    }

    @Test
    fun testGathering() = runTest {
        pushGatherer.registerPushToken(PUSH_TOKEN)
        pushGatherer.clearPushToken()

        threadSafePersistentStore.items shouldBe expected
        threadSafePersistentStore.items.size shouldBe 2
    }

    @Test
    fun testPushToken() = runTest {
        every { mockStringStorage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) } returns PUSH_TOKEN

        pushGatherer.getPushToken() shouldBe PUSH_TOKEN
    }

}