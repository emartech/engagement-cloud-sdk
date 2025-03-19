package com.emarsys.core.storage

import com.emarsys.AndroidEmarsysConfig
import com.emarsys.TestClass
import com.emarsys.core.log.Logger
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test

class AndroidSdkConfigStorageTests {
    private companion object {
        val CONFIG = AndroidEmarsysConfig(
            "testApplicationCode",
            "testMerchantId",
            "testSharedSecret",
            TestClass::class.java
        )
    }

    private lateinit var mockStringStorage: TypedStorageApi<String?>
    private lateinit var mockLogger: Logger
    private lateinit var json: Json
    private lateinit var storage: AndroidSdkConfigStorage

    @Before
    fun setUp() {
        mockStringStorage = mockk(relaxed = true)
        mockLogger = mockk(relaxed = true)
        json = JsonUtil.json
        storage = AndroidSdkConfigStorage(mockStringStorage, mockLogger, json)
    }

    @Test
    fun testStorage() = runTest {
        every { mockStringStorage.get(StorageConstants.SDK_CONFIG_KEY) } returns json.encodeToString(
            CONFIG
        )

        val result = storage.get(StorageConstants.SDK_CONFIG_KEY)

        result shouldBe CONFIG
    }

    @Test
    fun testPut_shouldNotCrashOnSerializationError() = runTest {
        val wrongString =
            """{"wrongKey: "wrongValue"}"""
        coEvery {
            mockStringStorage.put(
                StorageConstants.SDK_CONFIG_KEY,
                wrongString
            )
        } throws Exception()

        storage.put(StorageConstants.SDK_CONFIG_KEY, CONFIG)

        coVerify { mockLogger.error(tag = "SdkConfigStorage - put", throwable = any()) }
    }

    @Test
    fun testGet_shouldNotCrashOnSerializationError() = runTest {
        val wrongString =
            """{"wrongKey: "wrongValue"}"""
        coEvery { mockStringStorage.get(StorageConstants.SDK_CONFIG_KEY) } returns wrongString

        val result = storage.get(StorageConstants.SDK_CONFIG_KEY)

        result shouldBe null
        coVerify { mockLogger.error(tag = "SdkConfigStorage - get", throwable = any()) }
    }
}