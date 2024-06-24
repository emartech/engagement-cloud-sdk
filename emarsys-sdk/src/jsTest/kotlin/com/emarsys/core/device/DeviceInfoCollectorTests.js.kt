package com.emarsys.core.device

import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.TypedStorageApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeviceInfoCollectorTests {

    private companion object {
        const val TEST_UUID = "test uuid"
        const val STORED_ID = "stored hardware id"
        const val TIMEZONE = "+0600"
        const val BROWSER_NAME = "Chrome"
        const val BROWSER_VERSION = "160.5.12"
        const val APPLICATION_VERSION = "2.0.0"
        const val LANGUAGE = "testLanguage"
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
    private lateinit var mockStorage: TypedStorageApi<String?>
    private lateinit var mockUuidProvider: Provider<String>
    private lateinit var mockTimezoneProvider: Provider<String>
    private lateinit var mockApplicationVersionProvider: Provider<String>
    private lateinit var mockLanguageProvider: Provider<String>
    private lateinit var deviceInfoCollector: DeviceInfoCollector
    private val json: Json = Json

    @BeforeTest
    fun setup() {
        mockStorage = mock()
        every { mockStorage.put(any(), any()) } returns Unit
        mockUuidProvider = mock()
        every { mockUuidProvider.provide() } returns TEST_UUID
        mockTimezoneProvider = mock()
        every { mockTimezoneProvider.provide() } returns TIMEZONE
        mockApplicationVersionProvider = mock()
        every { mockApplicationVersionProvider.provide() } returns APPLICATION_VERSION
        mockWebPlatformInfoCollector = mock()
        every { mockWebPlatformInfoCollector.collect() } returns testWebPlatformInfo
        mockLanguageProvider = mock()
        every { mockLanguageProvider.provide() } returns LANGUAGE
        deviceInfoCollector = DeviceInfoCollector(
            mockUuidProvider,
            mockTimezoneProvider,
            mockWebPlatformInfoCollector,
            mockStorage,
            mockApplicationVersionProvider,
            mockLanguageProvider,
            json
        )
    }

    @Test
    fun collect_shouldReturn_deviceInfo() {
        val expectedDeviceInfo = DeviceInfo(
            platform = BROWSER_NAME,
            deviceModel = navigator.userAgent,
            sdkVersion = BuildConfig.VERSION_NAME,
            osVersion = BROWSER_VERSION,
            languageCode = LANGUAGE,
            timezone = TIMEZONE,
            applicationVersion = APPLICATION_VERSION,
        )

        val result = deviceInfoCollector.collect()

        result shouldBe json.encodeToString(expectedDeviceInfo)
    }

    @Test
    fun getHardwareId_shouldGenerateNewId_ifStoredId_isNull() {
        every { mockStorage.get(any()) } returns null
        deviceInfoCollector.getHardwareId() shouldBe TEST_UUID
    }

    @Test
    fun getHardwareId_shouldReturnStoredId_ifPresent() {
        every { mockStorage.get("hardwareId") } returns STORED_ID

        val testCollector = DeviceInfoCollector(
            mockUuidProvider,
            mockTimezoneProvider,
            mockWebPlatformInfoCollector,
            mockStorage,
            mockApplicationVersionProvider,
            mockLanguageProvider,
            json
        )

        testCollector.getHardwareId() shouldBe STORED_ID
    }
}