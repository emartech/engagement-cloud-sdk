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
import kotlinx.browser.window
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.w3c.dom.Storage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class WrapperInfoStorageTests {
    private companion object {
        const val WRAPPER_PLATFORM = "flutter"
        const val WRAPPER_VERSION = "1.0.0"
    }

    private val storage: Storage = window.localStorage
    private lateinit var mockSdkContextApi: SdkContextApi
    private lateinit var mockSdkLogger: Logger
    private lateinit var wrapperInfoStorage: TypedStorageApi<WrapperInfo?>

    @BeforeTest
    fun setup() {
        mockSdkContextApi = mock()
        every { mockSdkContextApi.sdkDispatcher } returns StandardTestDispatcher()
        mockSdkLogger = mock()
        everySuspend { mockSdkLogger.error(tag = any(), throwable = any()) } returns Unit
        wrapperInfoStorage =
            WrapperInfoStorage(storage, mockSdkContextApi, mockSdkLogger, JsonUtil.json)
    }

    @AfterTest
    fun tearDown() {
        storage.removeItem(StorageConstants.WRAPPER_INFO_KEY)
    }

    @Test
    fun testStorage() = runTest {
        val expectedWrapperInfo = WrapperInfo(WRAPPER_PLATFORM, WRAPPER_VERSION)
        wrapperInfoStorage.put(StorageConstants.WRAPPER_INFO_KEY, expectedWrapperInfo)

        val result = wrapperInfoStorage.get(StorageConstants.WRAPPER_INFO_KEY)

        result shouldBe expectedWrapperInfo
    }

    @Test
    fun testStorage_withNull() = runTest {
        val expectedWrapperInfo = WrapperInfo(WRAPPER_PLATFORM, WRAPPER_VERSION)
        wrapperInfoStorage.put(StorageConstants.WRAPPER_INFO_KEY, expectedWrapperInfo)
        wrapperInfoStorage.put(StorageConstants.WRAPPER_INFO_KEY, null)

        val result = wrapperInfoStorage.get(StorageConstants.WRAPPER_INFO_KEY)

        result shouldBe null
    }

    @Test
    fun testStorage_shouldNotCrashOnSerializationError() = runTest {
        val expectedWrapperInfo = WrapperInfo("unknown", "unknown")
        val wrongWrapperInfoString =
            """{"wrongPropertyName: "wrongPropertyValue","wrapperVersion":"1.0.0"}"""
        storage.setItem(StorageConstants.WRAPPER_INFO_KEY, wrongWrapperInfoString)

        val result = wrapperInfoStorage.get(StorageConstants.WRAPPER_INFO_KEY)

        result shouldBe expectedWrapperInfo
    }
}