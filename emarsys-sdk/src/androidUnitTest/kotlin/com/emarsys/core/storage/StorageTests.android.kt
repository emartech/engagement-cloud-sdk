package com.emarsys.core.storage

import android.content.SharedPreferences
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test

class StorageTests {
    
    private lateinit var mockPreference: SharedPreferences
    private lateinit var storage: Storage
    
    @BeforeTest
    fun setup() {
        mockPreference = mockk(relaxed = true)
        storage = Storage(mockPreference)
    }

    @Test
    fun testStorage_withString() {
        val key = "text"
        val expected = "testValue"
        
        every { mockPreference.getString(key, null) } returns expected
        
        storage.put(key, expected)
        
        val result: String? = storage.get(key)
        
        result shouldBe expected
    }
    
}