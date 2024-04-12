package com.emarsys.core.device

import com.emarsys.core.device.fakes.FakeStorage
import com.emarsys.core.device.fakes.FakeStringProvider
import com.emarsys.core.device.fakes.FakeWebPlatformInfoCollector
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

    private lateinit var fakeWebPlatformInfoCollector: WebPlatformInfoCollectorApi
    private lateinit var fakeStorage: FakeStorage
    private lateinit var fakeUuidProvider: FakeStringProvider
    private lateinit var fakeTimezoneProvider: FakeStringProvider
    private lateinit var fakeApplicationVersionProvider: FakeStringProvider
    private lateinit var deviceInfoCollector: DeviceInfoCollector

    @BeforeTest
    fun setup() {
        fakeStorage = FakeStorage()
        fakeUuidProvider = FakeStringProvider(TEST_UUID)
        fakeTimezoneProvider = FakeStringProvider(TIMEZONE)
        fakeApplicationVersionProvider = FakeStringProvider(APPLICATION_VERSION)
        fakeWebPlatformInfoCollector = FakeWebPlatformInfoCollector(testWebPlatformInfo)
        deviceInfoCollector = DeviceInfoCollector(
            fakeUuidProvider,
            fakeTimezoneProvider,
            fakeWebPlatformInfoCollector,
            fakeStorage,
            fakeApplicationVersionProvider
        )
    }

    @Test
    fun collect_shouldReturn_deviceInfo() {
        val expectedDeviceInfo = DeviceInfo(
            platform = BROWSER_NAME,
            deviceModel = navigator.userAgent,
            sdkVersion = BuildConfig.VERSION_NAME,
            osVersion = BROWSER_VERSION,
            languageCode = navigator.language,
            timezone = TIMEZONE,
            applicationVersion = APPLICATION_VERSION,
        )

        val result = deviceInfoCollector.collect()

        result shouldBe Json.encodeToString(expectedDeviceInfo)
    }

    @Test
    fun getHardwareId_shouldGenerateNewId_ifStoredId_isNull() {
        deviceInfoCollector.getHardwareId() shouldBe TEST_UUID
    }

    @Test
    fun getHardwareId_shouldReturnStoredId_ifPresent() {
        val testStorage = FakeStorage()
        testStorage.put("hardwareId", STORED_ID)

        val testCollector = DeviceInfoCollector(
            fakeUuidProvider,
            fakeTimezoneProvider,
            fakeWebPlatformInfoCollector,
            testStorage,
            fakeApplicationVersionProvider
        )

        testCollector.getHardwareId() shouldBe STORED_ID
    }
}