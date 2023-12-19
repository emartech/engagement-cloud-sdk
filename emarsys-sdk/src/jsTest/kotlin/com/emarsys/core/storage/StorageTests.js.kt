package com.emarsys.core.storage

import io.kotest.matchers.shouldBe
import kotlinx.browser.window
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class StorageTests {

    companion object {
        private const val KEY = "testKey"
    }
    private lateinit var storage: Storage

    @BeforeTest
    fun setup() {
        storage = Storage(window.localStorage)
    }
    
    @AfterTest
    fun tearDown() {
        window.localStorage.removeItem(KEY)
    }
    
    @Test
    fun testStorage() {
        val expected = "testValue"
        
        storage.put(KEY, expected)
        
        val result = storage.get(KEY)
        
        result shouldBe expected
    }

}
