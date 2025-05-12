package com.emarsys.core.storage

import com.emarsys.fake.FakeStringStorage
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.builtins.serializer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class StoreTests {

    private lateinit var store: Store<String>
    private lateinit var fakeStringStorage: FakeStringStorage

    @BeforeTest
    fun setUp() {
        fakeStringStorage = FakeStringStorage()
        store = Store(
            fakeStringStorage,
            JsonUtil.json,
            "testKey",
            String.serializer()
        )
    }

    @AfterTest
    fun tearDown() {
        fakeStringStorage.put("testKey", null)
    }

    @Test
    fun testValue_shouldBeNull() {
        val result = store.getValue(this, StoreTests::store)

        result shouldBe null
        fakeStringStorage.get("testKey") shouldBe null
    }

    @Test
    fun testValue_shouldReturn_theStoredValue_valueShouldBeStored_inStringStorage() {
        store.setValue(this, StoreTests::store, "testValue")

        val result = store.getValue(this, StoreTests::store)

        result shouldBe "testValue"
        fakeStringStorage.get("testKey") shouldNotBe null
    }
}