package com.emarsys.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.emarsys.applicationContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.wrapper.WrapperInfo
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Test

class WrapperInfoStorageTests {
    private companion object {
        const val WRAPPER_PLATFORM = "flutter"
        const val WRAPPER_VERSION = "1.0.0"
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var storage: WrapperInfoStorage

    @Before
    fun setUp() {
        sharedPreferences =
            applicationContext.getSharedPreferences("wrapperInfoTest", Context.MODE_PRIVATE)
        mockSdkContext = mockk(relaxed = true)
        every { mockSdkContext.sdkDispatcher } returns StandardTestDispatcher()
        storage = WrapperInfoStorage(
            sharedPreferences,
            mockSdkContext,
            mockk(relaxed = true),
            JsonUtil.json
        )
    }

    @After
    fun tearDown() {
        sharedPreferences.edit {
            clear()
        }
    }

    @Test
    fun testStorage() {
        val expectedWrapperInfo = WrapperInfo(WRAPPER_PLATFORM, WRAPPER_VERSION)
        storage.put(StorageConstants.WRAPPER_INFO_KEY, expectedWrapperInfo)

        val result = storage.get(StorageConstants.WRAPPER_INFO_KEY)

        result shouldBe expectedWrapperInfo
    }

    @Test
    fun testStorage_shouldNotCrashOnSerializationError() {
        val expectedWrapperInfo = WrapperInfo("unknown", "unknown")
        val wrongWrapperInfoString =
            """{"wrongPropertyName: "wrongPropertyValue","wrapperVersion":"1.0.0"}"""
        sharedPreferences.edit {
            putString(StorageConstants.WRAPPER_INFO_KEY, wrongWrapperInfoString)
        }

        val result = storage.get(StorageConstants.WRAPPER_INFO_KEY)

        result shouldBe expectedWrapperInfo
    }
}