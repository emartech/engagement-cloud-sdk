package com.emarsys.core.collections

import com.emarsys.core.storage.StorageApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.eq
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PersistentListTests {
    companion object {
        const val TEST_ID = "testId"
    }

    private lateinit var mockStorage: StorageApi
    private var storedList = mutableListOf("value1", "value2", "value3")
    private lateinit var persistentList: PersistentList<String>

    @BeforeTest
    fun setUp() {
        mockStorage = mock()
        every { mockStorage.get(eq(TEST_ID), any<KSerializer<Any>>()) } returns storedList

        persistentList = PersistentList(TEST_ID, mockStorage, String.serializer())
    }

    @AfterTest
    fun tearDown() = runTest {
        storedList = mutableListOf("value1", "value2", "value3")
    }

    @Test
    fun testSize() = runTest {
        persistentList.size shouldBe 3
    }

    @Test
    fun testClear() = runTest {
        teachStorage(emptyList())

        persistentList.clear()

        persistentList.size shouldBe 0
    }

    @Test
    fun testAddAll() = runTest {
        val elements = listOf("value4", "value5", "value6")
        storedList.addAll(elements)
        teachStorage(storedList)

        val result = persistentList.addAll(elements)

        persistentList.size shouldBe 6
        result shouldBe true
    }

    @Test
    fun testAddAllIndex() = runTest {
        val elements = listOf("value4", "value5", "value6")
        storedList.addAll(1, elements)
        teachStorage(storedList)

        val result = persistentList.addAll(1, elements)

        persistentList.size shouldBe 6
        result shouldBe true
    }

    @Test
    fun testAddIndex() = runTest {
        val element = "value4"
        storedList.add(1, element)
        teachStorage(storedList)

        persistentList.add(1, element)

        persistentList.size shouldBe 4
    }

    @Test
    fun testAdd() = runTest {
        val element = "value4"
        storedList.add(element)
        teachStorage(storedList)

        val result = persistentList.add(element)

        persistentList.size shouldBe 4
        result shouldBe true
    }

    @Test
    fun testGet() = runTest {
        val result = persistentList.get(1)

        result shouldBe "value2"
    }

    @Test
    fun testIsEmpty() = runTest {
        teachStorage(
            emptyList()
        )
        var result = persistentList.isEmpty()

        result shouldBe false

        persistentList.clear()
        result = persistentList.isEmpty()

        result shouldBe true
    }

    @Test
    fun testIterator() = runTest {
        val iterator = persistentList.iterator()

        while (iterator.hasNext()) {
            storedList.remove(iterator.next())
        }

        storedList shouldBe emptyList()
    }

    @Test
    fun testListIterator() = runTest {
        val iterator = persistentList.listIterator()

        while (iterator.hasNext()) {
            storedList.remove(iterator.next())
        }

        storedList shouldBe emptyList()
    }

    @Test
    fun testListIteratorIndex() = runTest {
        val iterator = persistentList.listIterator(1)

        while (iterator.hasNext()) {
            storedList.remove(iterator.next())
        }

        storedList shouldBe listOf("value1")
    }

    @Test
    fun testRemoveAtIndex() = runTest {
        storedList.removeAt(1)
        teachStorage(storedList)

        val result = persistentList.removeAt(1)

        persistentList.size shouldBe 2
        result shouldBe "value2"
    }

    @Test
    fun testSubList() = runTest {
        val result = persistentList.subList(0, 2)

        result shouldBe listOf("value1", "value2")
    }

    @Test
    fun testSetIndex() = runTest {
        storedList[1] = "value4"
        teachStorage(storedList)

        val result = persistentList.set(1, "value4")

        persistentList.size shouldBe 3
        result shouldBe "value2"
    }

    @Test
    fun testRetainAll() = runTest {
        val elements = listOf("value1", "value3")
        storedList.retainAll(elements)
        teachStorage(storedList)

        val result = persistentList.retainAll(elements)

        persistentList.size shouldBe 2
        result shouldBe true
    }

    @Test
    fun testRemoveAll() = runTest {
        val elements = listOf("value1", "value3")
        storedList.removeAll(elements)
        teachStorage(storedList)

        val result = persistentList.removeAll(elements)

        persistentList.size shouldBe 1
        result shouldBe true
    }

    @Test
    fun testRemove() = runTest {
        val element = "value2"
        storedList.remove(element)
        teachStorage(storedList)

        val result = persistentList.remove(element)

        persistentList.size shouldBe 2
        result shouldBe true
    }

    @Test
    fun testLastIndexOf() = runTest {
        val elements = listOf("value1", "value4")
        storedList.addAll(elements)
        teachStorage(storedList)
        persistentList.addAll(elements)

        val result = persistentList.lastIndexOf("value1")

        result shouldBe 3
    }

    @Test
    fun testIndexOf() = runTest {
        val result = persistentList.indexOf("value2")

        result shouldBe 1
    }

    @Test
    fun testContainsAll() = runTest {
        val elements = listOf("value1", "value2")

        val result = persistentList.containsAll(elements)

        result shouldBe true
    }

    @Test
    fun testContains() = runTest {
        val element = "value1"

        val result = persistentList.contains(element)

        result shouldBe true
    }

    @Test
    fun testSetGetOperator() = runTest {
        val element = "value4"
        storedList[1] = element
        teachStorage(storedList)

        persistentList[1] = element

        val result = persistentList[1]

        result shouldBe element
    }

    @Test
    fun testSecondaryConstructor() = runTest {
        val value1 = "otherValue1"
        val value2 = "otherValue2"
        val elements = listOf(value1, value2)
        every { mockStorage.get(eq("testId2"), any<KSerializer<Any>>()) } returns elements
        every { mockStorage.put(eq("testId2"), any<KSerializer<Any>>(), eq(elements)) } returns Unit

        val persistentList2 =
            persistentListOf("testId2", mockStorage, String.serializer(), value1, value2)

        persistentList2.size shouldBe 2
        persistentList2[0] shouldBe value1
        persistentList2[1] shouldBe value2
    }

    @Test
    fun testSecondaryConstructorWithEmptyVararg() = runTest {
        val elements = listOf<String>()
        every { mockStorage.get(eq("testId2"), any<KSerializer<Any>>()) } returns elements
        every { mockStorage.put(eq("testId2"), any<KSerializer<Any>>(), eq(elements)) } returns Unit

        val persistentList2 = persistentListOf("testId2", mockStorage, String.serializer())

        persistentList2.size shouldBe 0
    }

    private fun teachStorage(elements: List<String>) {
        every { mockStorage.put(eq(TEST_ID), any<KSerializer<Any>>(), eq(elements)) } returns Unit
    }

}