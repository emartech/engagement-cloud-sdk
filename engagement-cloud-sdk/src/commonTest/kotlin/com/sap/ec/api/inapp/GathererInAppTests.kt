package com.sap.ec.api.inapp

import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.storage.StorageApi
import dev.mokkery.MockMode
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class GathererInAppTests {
    private companion object {
        const val TEST_STORE_ID = "testStoreId"
    }

    private lateinit var gathererInApp: GathererInApp
    private lateinit var mockStorage: StorageApi
    private lateinit var threadSafePersistentStore: ThreadSafePersistentStoreApi<InAppCall>
    private lateinit var inAppConfig: InAppConfig

    @BeforeTest
    fun setup() = runTest {
        inAppConfig = InAppConfig()
        mockStorage = mock(MockMode.autofill)
        threadSafePersistentStore = ThreadSafePersistentStore(
            TEST_STORE_ID,
            mockStorage,
            InAppCall.serializer()
        )
        gathererInApp = GathererInApp(inAppConfig, threadSafePersistentStore)
    }

    @Test
    fun testIsPaused() = runTest {
        gathererInApp.isPaused shouldBe false

        inAppConfig.inAppDnd = true

        gathererInApp.isPaused shouldBe true
    }

    @Test
    fun testPause_shouldAddCallToContext() = runTest {
        val testCall = InAppCall.Pause()

        gathererInApp.pause()
        threadSafePersistentStore.items.contains(testCall) shouldBe true
    }

    @Test
    fun testResume_shouldAddCallToContext() = runTest {
        val testCall = InAppCall.Resume()

        gathererInApp.resume()

        threadSafePersistentStore.items.contains(testCall) shouldBe true
        threadSafePersistentStore.items.size shouldBe 1
    }
}