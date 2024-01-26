package com.emarsys.core.storage

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class StringFakeStorage: StorageApi<String?> {

    val store = mutableMapOf<String, String>()
    override fun put(key: String, value: String?) {
        if (value == null) {
            store.remove(key)
        } else {
            store.put(key, value)
        }
    }

    override fun get(key: String): String? {
        return store.get(key)
    }

}

@Serializable
data class TestData(val value: String)

class StorageTests {

    companion object {
        val key = "testKey"
    }

    val stringStorage = StringFakeStorage()
    lateinit var storage: Storage

    @BeforeTest
    fun setup() = runTest {
        val json = Json { encodeDefaults = true }
        storage = Storage(stringStorage, json)
    }

    @Test
    fun testStorage_withBoolean() = runTest {
        val expected = true

        storage.put(key, expected)

        val result: Boolean? = storage.get(key)

        result shouldBe expected
    }

    @Test
    fun testStorage_withInt() = runTest {
        val expected = 42

        storage.put(key, expected)

        val result: Int? = storage.get(key)

        result shouldBe expected
    }

    @Test
    fun testStorage_withDouble() = runTest {
        val expected = 42f

        storage.put(key, expected)

        val result: Double? = storage.get(key)

        result shouldBe expected
    }

    @Test
    fun testStorage_withList() = runTest {
        val expected = listOf("test")

        storage.put(key, expected)

        val result: List<String>? = storage.get(key)

        result shouldBe expected
    }

    @Test
    fun testStorage_withMap() = runTest {
        val expected = mapOf("testKey" to "testValue")

        storage.put(key, expected)

        val result: Map<String, String>? = storage.get(key)

        result shouldBe expected
    }

    @Test
    fun testStorage_withSerializableDataClass() = runTest {
        val expected = TestData("test")

        storage.put(key, expected)

        val result: TestData? = storage.get(key)

        result shouldBe expected
    }

}