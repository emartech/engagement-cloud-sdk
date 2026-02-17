package com.sap.ec.core.storage

import com.sap.ec.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.BeforeTest
import kotlin.test.Test

@Serializable
data class TestData(val value: String)

@Serializable
sealed interface SealedTestData {
    @Serializable
    data class DataType1(val value: String) : SealedTestData

    @Serializable
    data class DataType2(val value: Int) : SealedTestData
}

class StorageTests {
    companion object {
        const val KEY = "testKey"
    }

    private lateinit var mockStringStorage: StringStorageApi
    private val json = JsonUtil.json
    private lateinit var storage: StorageApi

    @BeforeTest
    fun setUp() {
        mockStringStorage = mock()
        storage = Storage(mockStringStorage, json)
    }

    @Test
    fun testStorage_withBoolean() = runTest {
        val expected = true
        val encoded: String = json.encodeToString(expected)

        every { mockStringStorage.put(KEY, encoded) } returns Unit
        every { mockStringStorage.get(KEY) } returns encoded

        storage.put(KEY, Boolean.serializer(), expected)

        val result: Boolean? = storage.get(KEY, Boolean.serializer())

        result shouldBe expected
    }

    @Test
    fun testStorage_withInt() = runTest {
        val expected = 42
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(KEY, encoded) } returns Unit
        every { mockStringStorage.get(KEY) } returns encoded

        storage.put(KEY, Int.serializer(), expected)

        val result: Int? = storage.get(KEY, Int.serializer())

        result shouldBe expected
    }

    @Test
    fun testStorage_withDouble() = runTest {
        val expected = 42.0
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(KEY, encoded) } returns Unit
        every { mockStringStorage.get(KEY) } returns encoded

        storage.put(KEY, Double.serializer(), expected)

        val result: Double? = storage.get(KEY, Double.serializer())

        result shouldBe expected
    }

    @Test
    fun testStorage_withString() = runTest {
        val expected = "testValue"
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(KEY, encoded) } returns Unit
        every { mockStringStorage.get(KEY) } returns encoded

        storage.put(KEY, String.serializer(), expected)

        val result: String? = storage.get(KEY, String.serializer())

        result shouldBe expected
    }

    @Test
    fun testStorage_withList() = runTest {
        val expected = listOf("test")
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(KEY, encoded) } returns Unit
        every { mockStringStorage.get(KEY) } returns encoded

        storage.put(KEY, ListSerializer(String.serializer()), expected)

        val result: List<String>? = storage.get(KEY, ListSerializer(String.serializer()))

        result shouldBe expected
    }

    @Test
    fun testStorage_withMap() = runTest {
        val expected = mapOf("testKey" to "testValue")
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(KEY, encoded) } returns Unit
        every { mockStringStorage.get(KEY) } returns encoded

        storage.put(KEY, MapSerializer(String.serializer(), String.serializer()), expected)

        val result: Map<String, String>? =
            storage.get(KEY, MapSerializer(String.serializer(), String.serializer()))

        result shouldBe expected
    }

    @Test
    fun testStorage_withSerializableDataClass() = runTest {
        val expected = TestData("test")
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(KEY, encoded) } returns Unit
        every { mockStringStorage.get(KEY) } returns encoded

        storage.put(KEY, TestData.serializer(), expected)

        val result: TestData? = storage.get(KEY, TestData.serializer())

        result shouldBe expected
    }

    @Test
    fun testStorage_withSerializableSealedClasses() = runTest {
        val expected = listOf(SealedTestData.DataType1("testValue"), SealedTestData.DataType2(42))
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(KEY, encoded) } returns Unit
        every { mockStringStorage.get(KEY) } returns encoded

        storage.put(KEY, ListSerializer(SealedTestData.serializer()), expected)

        val result: List<SealedTestData>? =
            storage.get(KEY, ListSerializer(SealedTestData.serializer()))

        result shouldBe expected
    }

}