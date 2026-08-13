package com.sap.ec.api.inapp

import com.sap.ec.core.collections.ThreadSafePersistentStore
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.storage.StorageApi
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlin.test.BeforeTest
import kotlin.test.Test

class InAppInternalTests {
    private companion object {
        const val TEST_STORE_ID = "testStoreId"
    }

    private lateinit var mockStorage: StorageApi
    private lateinit var threadSafePersistentStore: ThreadSafePersistentStoreApi<InAppCall>

    @BeforeTest
    fun setup() {
        mockStorage = mock(MockMode.autofill)
        threadSafePersistentStore = createSafeStore()
    }

    @Test
    fun testPause_shouldSetDND_true() = runTest {
        val inAppInternal = createInappInternal(safeStore = threadSafePersistentStore)
        inAppInternal.isPaused shouldBe false

        inAppInternal.pause()

        inAppInternal.isPaused shouldBe true
    }

    @Test
    fun testResume_shouldSetDND_false() = runTest {
        val inAppInternal = createInappInternal(true, threadSafePersistentStore)

        inAppInternal.resume()

        inAppInternal.isPaused shouldBe false
    }

    @Test
    fun testActivate_shouldSetInAppDndToTheLastInAppCallValue_if_lastCallIsPause() = runTest {
        val lastInAppCall = InAppCall.Pause()
        val inAppCallList = mutableListOf(InAppCall.Resume(), InAppCall.Pause(), lastInAppCall)
        every {
            mockStorage.get(TEST_STORE_ID, any<KSerializer<List<Any>>>())
        } returns inAppCallList
        val inAppConfig: InAppConfigApi = InAppConfig(false)
        val spyInAppConfig = spy(inAppConfig)
        val inAppInternal = InAppInternal(spyInAppConfig, createSafeStore())

        inAppInternal.activate()

        inAppInternal.isPaused shouldBe true
        verify { spyInAppConfig.inAppDnd = true }
    }

    @Test
    fun testActivate_shouldSetInAppDndToTheLastInAppCallValue_if_lastCallIsResume() = runTest {
        val lastInAppCall = InAppCall.Resume()
        val inAppCallList = mutableListOf(InAppCall.Resume(), InAppCall.Pause(), lastInAppCall)
        every {
            mockStorage.get(TEST_STORE_ID, any<KSerializer<List<Any>>>())
        } returns inAppCallList
        val inAppConfig: InAppConfigApi = InAppConfig(true)
        val spyInAppConfig = spy(inAppConfig)
        val inAppInternal = InAppInternal(spyInAppConfig, createSafeStore())

        inAppInternal.activate()

        inAppInternal.isPaused shouldBe false
        verify { spyInAppConfig.inAppDnd = false }
    }

    @Test
    fun testActivate_shouldNotSetInAppDnd_if_callsListIsEmpty() = runTest {
        val mockInAppConfig = mock<InAppConfigApi>()
        every {
            mockStorage.get(TEST_STORE_ID, any<KSerializer<List<Any>>>())
        } returns mutableListOf()

        val inAppInternal = InAppInternal(mockInAppConfig, createSafeStore())

        inAppInternal.activate()

        verify(VerifyMode.exactly(0)) { mockInAppConfig.inAppDnd = any<Boolean>() }
    }

    private fun createInappInternal(
        inappDnd: Boolean = false,
        safeStore: ThreadSafePersistentStoreApi<InAppCall>
    ): InAppInternal =
        InAppInternal(InAppConfig(inappDnd), safeStore)

    private fun createSafeStore(storage: StorageApi = mockStorage): ThreadSafePersistentStoreApi<InAppCall> =
        ThreadSafePersistentStore(TEST_STORE_ID, storage, InAppCall.serializer())
}