package com.emarsys.core.device

import com.emarsys.SdkConstants
import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.wrapper.WrapperInfo
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.browser.window
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeviceInfoCollectorTests {

    private companion object {
        const val TIMEZONE = "+0600"
        const val BROWSER_NAME = "chrome"
        const val BROWSER_VERSION = "160.5.12"
        const val APPLICATION_VERSION = "2.0.0"
        const val LANGUAGE = "testLanguage"
        const val CLIENT_ID = "stored client id"
        const val WRAPPER_PLATFORM = "flutter"
        const val WRAPPER_VERSION = "1.0.0"

        val navigator = window.navigator
        val testWebPlatformInfo = WebPlatformInfo(
            null,
            false,
            osName = "Macintosh",
            osVersion = "OS X 11.0.5",
            browserName = BROWSER_NAME,
            browserVersion = BROWSER_VERSION
        )
    }

    private lateinit var mockWebPlatformInfoCollector: WebPlatformInfoCollectorApi
    private lateinit var mockClientIdProvider: Provider<String>
    private lateinit var mockTimezoneProvider: Provider<String>
    private lateinit var mockApplicationVersionProvider: Provider<String>
    private lateinit var mockLanguageProvider: Provider<String>
    private lateinit var mockWrapperInfoStorage: TypedStorageApi
    private lateinit var deviceInfoCollector: DeviceInfoCollector
    private val json: Json = JsonUtil.json

    @BeforeTest
    fun setup() {
        mockClientIdProvider = mock()
        every { mockClientIdProvider.provide() } returns CLIENT_ID
        mockTimezoneProvider = mock()
        every { mockTimezoneProvider.provide() } returns TIMEZONE
        mockApplicationVersionProvider = mock()
        every { mockApplicationVersionProvider.provide() } returns APPLICATION_VERSION
        mockWebPlatformInfoCollector = mock()
        every { mockWebPlatformInfoCollector.collect() } returns testWebPlatformInfo
        mockLanguageProvider = mock()
        every { mockLanguageProvider.provide() } returns LANGUAGE
        mockWrapperInfoStorage = mock()
        everySuspend { mockWrapperInfoStorage.get(StorageConstants.WRAPPER_INFO_KEY, WrapperInfo.serializer()) } returns null

        deviceInfoCollector = DeviceInfoCollector(
            mockClientIdProvider,
            mockTimezoneProvider,
            mockWebPlatformInfoCollector,
            mockApplicationVersionProvider,
            mockLanguageProvider,
            mockWrapperInfoStorage,
            json,
        )
    }

    @Test
    fun collect_shouldReturn_deviceInfo() = runTest {
        val expectedDeviceInfo = DeviceInfo(
            platform = BROWSER_NAME,
            platformCategory = SdkConstants.WEB_PLATFORM_CATEGORY,
            platformWrapper = null,
            platformWrapperVersion = null,
            applicationVersion = APPLICATION_VERSION,
            deviceModel = navigator.userAgent,
            osVersion = BROWSER_VERSION,
            sdkVersion = BuildConfig.VERSION_NAME,
            language = LANGUAGE,
            timezone = TIMEZONE,
            clientId = CLIENT_ID
        )

        val result = deviceInfoCollector.collect()

        result shouldBe json.encodeToString(expectedDeviceInfo)
    }

    @Test
    fun collect_shouldReturn_deviceInfo_whenWrapper() = runTest {
        everySuspend { mockWrapperInfoStorage.get(StorageConstants.WRAPPER_INFO_KEY, WrapperInfo.serializer()) } returns WrapperInfo(
            platformWrapper = WRAPPER_PLATFORM,
            wrapperVersion = WRAPPER_VERSION
        )

        val expectedDeviceInfo = DeviceInfo(
            platform = BROWSER_NAME,
            platformCategory = SdkConstants.WEB_PLATFORM_CATEGORY,
            platformWrapper = WRAPPER_PLATFORM,
            platformWrapperVersion = WRAPPER_VERSION,
            applicationVersion = APPLICATION_VERSION,
            deviceModel = navigator.userAgent,
            osVersion = BROWSER_VERSION,
            sdkVersion = BuildConfig.VERSION_NAME,
            language = LANGUAGE,
            timezone = TIMEZONE,
            clientId = CLIENT_ID
        )

        val result = deviceInfoCollector.collect()

        result shouldBe json.encodeToString(expectedDeviceInfo)
    }
}