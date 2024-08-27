package com.emarsys.core.cache

import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.toByteArray
import platform.Foundation.NSFileManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class IosFileCacheTests {
    private companion object {
        const val TEST_FILENAME = "testfileName"
    }

    private lateinit var fileManager: NSFileManager
    private lateinit var cache: IosFileCache

    @BeforeTest
    fun setUp() {
        fileManager = NSFileManager.defaultManager
        cache = IosFileCache(fileManager)
    }

    @AfterTest
    fun tearDown() {
        cache.remove(TEST_FILENAME)
    }

    @Test
    fun testCache() {
        val expected = "testFileContent".toByteArray()

        cache.cache(TEST_FILENAME, expected)

        cache.get(TEST_FILENAME) shouldBe expected
    }

    @Test
    fun testRemove_shouldRemoveFile() {
        val expected = "testFileContent".toByteArray()

        cache.cache(TEST_FILENAME, expected)

        cache.remove(TEST_FILENAME)

        val result = cache.get(TEST_FILENAME)

        result shouldBe null
    }
}