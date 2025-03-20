package com.emarsys.core.storage

import io.kotest.matchers.shouldBe
import kotlinx.browser.window
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class StringStorageTests {

    companion object {
        private const val KEY = "testKey"
    }
    private lateinit var storage: StringStorage

    @BeforeTest
    fun setup() {
        storage = StringStorage(window.localStorage)
    }
    
    @AfterTest
    fun tearDown() {
        window.localStorage.removeItem(KEY)
    }
    
    @Test
    fun testStorage() = runTest {
        val expected = "testValue"
        
        storage.put(KEY, expected)
        
        val result = storage.get(KEY)
        
        result shouldBe expected
    }

}
