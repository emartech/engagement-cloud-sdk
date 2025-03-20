package com.emarsys.core.storage

import android.content.SharedPreferences
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class StringStorageTests {

    private lateinit var mockPreference: SharedPreferences
    private lateinit var storage: StringStorage

    @BeforeTest
    fun setup() {
        mockPreference = mockk(relaxed = true)
        storage = StringStorage(mockPreference)
    }

    @Test
    fun testStorage_withString() = runTest {
        val key = "text"
        val expected = "testValue"

        every { mockPreference.getString(key, null) } returns expected

        storage.put(key, expected)

        val result: String? = storage.get(key)

        result shouldBe expected
    }

}