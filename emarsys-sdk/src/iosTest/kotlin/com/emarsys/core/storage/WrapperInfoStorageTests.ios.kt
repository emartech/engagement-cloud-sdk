package com.emarsys.core.storage

import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.wrapper.WrapperInfo
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSUserDefaults
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WrapperInfoStorageTests {
    private companion object {
        const val WRAPPER_PLATFORM = "flutter"
        const val WRAPPER_VERSION = "1.0.0"
    }

    private lateinit var userDefaults: NSUserDefaults
    private lateinit var mockSdkContextApi: SdkContextApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var storage: TypedStorageApi<WrapperInfo?>

    @BeforeTest
    fun setUp() = runTest {
        mockSdkContextApi = mock()
        every { mockSdkContextApi.sdkDispatcher } returns StandardTestDispatcher()
        mockSdkLogger = mock()
        everySuspend { mockSdkLogger.error(tag = any(), throwable = any()) } returns Unit
        userDefaults = NSUserDefaults(suiteName = "testSuite")
        storage = WrapperInfoStorage(userDefaults, mockSdkContextApi, mockSdkLogger, JsonUtil.json)
    }

    @AfterTest
    fun tearDown() {
        userDefaults.removeObjectForKey(StorageConstants.WRAPPER_INFO_KEY)
    }

    @Test
    fun testStorage() = runTest {
        val expectedWrapperInfo = WrapperInfo(WRAPPER_PLATFORM, WRAPPER_VERSION)
        storage.put(StorageConstants.WRAPPER_INFO_KEY, expectedWrapperInfo)

        val result = storage.get(StorageConstants.WRAPPER_INFO_KEY)

        result shouldBe expectedWrapperInfo
    }

    @Test
    fun testStorage_withNull() = runTest {
        storage.put(StorageConstants.WRAPPER_INFO_KEY, null)

        val result = storage.get(StorageConstants.WRAPPER_INFO_KEY)

        result shouldBe null
    }

    @Test
    fun testStorage_shouldNotCrashOnSerializationError() = runTest {
        val expectedWrapperInfo = WrapperInfo("unknown", "unknown")
        val wrongWrapperInfoString =
            """{"wrongPropertyName: "wrongPropertyValue","wrapperVersion":"1.0.0"}"""
        userDefaults.setObject(wrongWrapperInfoString, StorageConstants.WRAPPER_INFO_KEY)

        val result = storage.get(StorageConstants.WRAPPER_INFO_KEY)

        result shouldBe expectedWrapperInfo
    }

}