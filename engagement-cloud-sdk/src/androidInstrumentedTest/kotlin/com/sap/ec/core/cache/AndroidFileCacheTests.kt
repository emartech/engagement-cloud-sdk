package com.sap.ec.core.cache

import android.content.Context
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import okio.Path.Companion.toPath
import okio.buffer
import okio.fakefilesystem.FakeFileSystem
import okio.use
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File


class AndroidFileCacheTests {
    private companion object {
        const val TEST_FILENAME = "testfileName"
        const val CACHE_DIR = "emarsys_sdk_cache"
        val PATH = "$CACHE_DIR/$TEST_FILENAME".toPath()
    }
    private lateinit var context: Context
    private lateinit var fakeFileSystem: FakeFileSystem
    private lateinit var cache: AndroidFileCache

    @Before
    fun setUp() {
        fakeFileSystem = FakeFileSystem()
        context = mockk(relaxed = true)
        every { context.cacheDir } returns File(CACHE_DIR)
        cache = AndroidFileCache(context, fakeFileSystem)

        fakeFileSystem.createDirectories(CACHE_DIR.toPath())
    }

    @After
    fun tearDown() {
        fakeFileSystem.checkNoOpenFiles()
    }

    @Test
    fun testGet_returnsResultFromCache() {
        val expected = "testFileContent".toByteArray()

        fakeFileSystem.sink(PATH).buffer().use {
            it.write(expected)
        }

        val result = cache.get(TEST_FILENAME)

        result shouldBe expected
    }

    @Test
    fun testGet_shouldNotThrow_whenDirectoryDoesNotExists() {
        fakeFileSystem.delete(CACHE_DIR.toPath())

        val result = cache.get(TEST_FILENAME)

        result shouldBe null
    }

    @Test
    fun testCache() {
        val expected = "testFileContent".toByteArray()

        cache.cache(TEST_FILENAME, expected)

        val result = fakeFileSystem.source(PATH).use {
            it.buffer().readByteArray()
        }

        result shouldBe expected
    }

    @Test
    fun testRemove() {
        val content = "testFileContent".toByteArray()

        fakeFileSystem.sink(PATH).buffer().use {
            it.write(content)
        }

        cache.remove(TEST_FILENAME)

        fakeFileSystem.exists(PATH) shouldBe false
    }
}