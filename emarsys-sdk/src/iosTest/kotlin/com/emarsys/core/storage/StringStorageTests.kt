package com.emarsys.core.storage

import io.kotest.matchers.shouldBe
import platform.Foundation.NSUserDefaults
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class StringStorageTests {
    private companion object {
        const val TEST_KEY = "testKey"
        const val TEST_VALUE = "testValue"
    }

    private lateinit var userDefaults: NSUserDefaults
    private lateinit var storage: StringStorage

    @BeforeTest
    fun setUp() {
        userDefaults = NSUserDefaults(suiteName = StorageConstants.SUITE_NAME)
        storage = StringStorage(userDefaults)
    }

    @AfterTest
    fun tearDown() {
        userDefaults.removeObjectForKey(TEST_KEY)
    }

    @Test
    fun testStorage_usesInjectedUserDefaults() {
        storage.put(TEST_KEY, TEST_VALUE)

        storage.get(TEST_KEY) shouldBe TEST_VALUE
    }

    @Test
    fun testStorage_withNull() {
        storage.put(TEST_KEY, TEST_VALUE)
        storage.put(TEST_KEY, null)

        storage.get(TEST_KEY) shouldBe null
    }

    @Test
    fun testStorage_shouldReturnNull_whenNoValueWasStored() {
        storage.get(TEST_KEY) shouldBe null
    }
}