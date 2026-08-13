package com.sap.ec.core.collections

import com.sap.ec.core.storage.StorageApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ThreadSafePersistentStoreTests {
    companion object {
        const val TEST_ID = "testId"
    }

    private lateinit var mockStorage: StorageApi
    private var storedList = mutableListOf("value1", "value2", "value3")
    private lateinit var threadSafePersistentStore: ThreadSafePersistentStoreApi<String>

    @BeforeTest
    fun setUp() {
        mockStorage = mock()
    }

    @AfterTest
    fun tearDown() = runTest {
        storedList = mutableListOf("value1", "value2", "value3")
    }

    @Test
    fun constructor_shouldInitStore_withStoredList_ifPresent() = runTest {
        teachStorageGet(storedList)

        threadSafePersistentStore = ThreadSafePersistentStore(TEST_ID, mockStorage, String.serializer())

        threadSafePersistentStore.items.size shouldBe 3
        threadSafePersistentStore.items shouldBe storedList
        verify(VerifyMode.exactly(0)) { mockStorage.put(TEST_ID, any<KSerializer<Any>>(), any()) }
    }

    @Test
    fun constructor_shouldInitStore_withEmptyList_ifStoredList_notFound() = runTest {
        teachStorageGet(null)

        threadSafePersistentStore = ThreadSafePersistentStore(TEST_ID, mockStorage, String.serializer())

        threadSafePersistentStore.items.size shouldBe 0
        threadSafePersistentStore.items shouldBe emptyList()
        verify(VerifyMode.exactly(0)) { mockStorage.put(TEST_ID, any<KSerializer<Any>>(), any()) }
    }

    @Test
    fun add_shouldAddElementToItems_andCallPut_onStore() = runTest {
        val testValue = "new value"
        teachStorageGet(null)
        teachStoragePut(listOf(testValue))

        threadSafePersistentStore = ThreadSafePersistentStore(TEST_ID, mockStorage, String.serializer())

        threadSafePersistentStore.add(testValue)

        threadSafePersistentStore.items.size shouldBe 1
        threadSafePersistentStore.items shouldBe listOf(testValue)
        verify(VerifyMode.exactly(1)) {
            mockStorage.put(TEST_ID, any<KSerializer<Any>>(), listOf(testValue))
        }
    }

    @Test
    fun add_shouldAddElementToItems_andCallPutOnStore_withAllItems() = runTest {
        val testValue = "new value"
        teachStorageGet(storedList)
        teachStoragePut(storedList + listOf(testValue))

        threadSafePersistentStore = ThreadSafePersistentStore(TEST_ID, mockStorage, String.serializer())

        threadSafePersistentStore.add(testValue)

        threadSafePersistentStore.items.size shouldBe 4
        threadSafePersistentStore.items shouldBe storedList + listOf(testValue)
        verify(VerifyMode.exactly(1)) {
            mockStorage.put(TEST_ID, any<KSerializer<Any>>(), storedList + listOf(testValue))
        }
    }

    @Test
    fun dequeue_shouldRemoveItems_andCallPutOnStore_whenDone() = runTest {
        teachStorageGet(storedList)
        teachStoragePut(emptyList())
        val processedItems = mutableListOf<String>()

        threadSafePersistentStore = ThreadSafePersistentStore(TEST_ID, mockStorage, String.serializer())

        threadSafePersistentStore.dequeue { item -> processedItems.add(item) }

        threadSafePersistentStore.items.size shouldBe 0
        processedItems.size shouldBe 3
        processedItems shouldBe storedList
    }

    private fun teachStoragePut(elements: List<String>) {
        every { mockStorage.put(TEST_ID, any<KSerializer<Any>>(), elements) } returns Unit
    }

    private fun teachStorageGet(elements: List<String>?) {
        every { mockStorage.get(TEST_ID, any<KSerializer<Any>>()) } returns elements
    }

}