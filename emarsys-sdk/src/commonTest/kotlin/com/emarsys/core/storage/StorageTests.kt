package com.emarsys.core.storage

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.mock.Mock
import org.kodein.mock.tests.TestsWithMocks
import kotlin.test.Test

@Serializable
data class TestData(val value: String)

@Serializable
sealed interface SealedTestData {

    @Serializable
    data class DataType1(val value: String): SealedTestData

    @Serializable
    data class DataType2(val value: Int): SealedTestData
}

class StorageTests: TestsWithMocks() {

    companion object {
        const val key = "testKey"
    }

    override fun setUpMocks() = injectMocks(mocker)

    @Mock
    lateinit var mockStringStorage: TypedStorageApi<String?>

    private val json = Json { encodeDefaults = true }

    private val storage: StorageApi by withMocks {
        Storage(mockStringStorage, json)
    }

    @Test
    fun testStorage_withBoolean() = runTest {
        val expected = true
        val encoded: String = json.encodeToString(expected)

        every { mockStringStorage.put(key, encoded) } returns Unit
        every { mockStringStorage.get(key) } returns encoded

        storage.put(key, Boolean.serializer(), expected)

        val result: Boolean? = storage.get(key, Boolean.serializer())

        result shouldBe expected
    }

    @Test
    fun testStorage_withInt() = runTest {
        val expected = 42
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(key, encoded) } returns Unit
        every { mockStringStorage.get(key) } returns encoded

        storage.put(key, Int.serializer(), expected)

        val result: Int? = storage.get(key, Int.serializer())

        result shouldBe expected
    }

    @Test
    fun testStorage_withDouble() = runTest {
        val expected = 42.0
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(key, encoded) } returns Unit
        every { mockStringStorage.get(key) } returns encoded

        storage.put(key, Double.serializer(), expected)

        val result: Double? = storage.get(key, Double.serializer())

        result shouldBe expected
    }

    @Test
    fun testStorage_withString() = runTest {
        val expected = "testValue"
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(key, encoded) } returns Unit
        every { mockStringStorage.get(key) } returns encoded

        storage.put(key, String.serializer(), expected)

        val result: String? = storage.get(key, String.serializer())

        result shouldBe expected
    }

    @Test
    fun testStorage_withList() = runTest {
        val expected = listOf("test")
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(key, encoded) } returns Unit
        every { mockStringStorage.get(key) } returns encoded

        storage.put(key, ListSerializer(String.serializer()), expected)

        val result: List<String>? = storage.get(key, ListSerializer(String.serializer()))

        result shouldBe expected
    }

    @Test
    fun testStorage_withMap() = runTest {
        val expected = mapOf("testKey" to "testValue")
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(key, encoded) } returns Unit
        every { mockStringStorage.get(key) } returns encoded

        storage.put(key, MapSerializer(String.serializer(), String.serializer()), expected)

        val result: Map<String, String>? = storage.get(key, MapSerializer(String.serializer(), String.serializer()))

        result shouldBe expected
    }

    @Test
    fun testStorage_withSerializableDataClass() = runTest {
        val expected = TestData("test")
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(key, encoded) } returns Unit
        every { mockStringStorage.get(key) } returns encoded

        storage.put(key, TestData.serializer(), expected)

        val result: TestData? = storage.get(key, TestData.serializer())

        result shouldBe expected
    }

    @Test
    fun testStorage_withSerializableSealedClasses() = runTest {
        val expected = listOf(SealedTestData.DataType1("testValue"), SealedTestData.DataType2(42))
        val encoded = json.encodeToString(expected)

        every { mockStringStorage.put(key, encoded) } returns Unit
        every { mockStringStorage.get(key) } returns encoded

        storage.put(key, ListSerializer(SealedTestData.serializer()), expected)

        val result: List<SealedTestData>? = storage.get(key, ListSerializer(SealedTestData.serializer()))

        result shouldBe expected
    }

}