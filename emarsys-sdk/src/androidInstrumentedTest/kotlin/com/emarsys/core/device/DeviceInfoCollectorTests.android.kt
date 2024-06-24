package com.emarsys.core.device

import android.os.Build
import com.emarsys.core.providers.Provider
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test

class DeviceInfoCollectorTests {

    private companion object {
        const val LANGUAGE = "en-US"
        const val APP_VERSION = "2.0"
        const val HW_ID = "test uuid"
        const val TIMEZONE = "+0300"
    }

    private lateinit var mockLanguageProvider: Provider<String>
    private lateinit var mockTimezoneProvider: Provider<String>
    private lateinit var mockApplicationVersionProvider: Provider<String>
    private lateinit var mockHardwareIdProvider: Provider<String>
    private lateinit var deviceInfoCollector: DeviceInfoCollector
    private val json = Json

    @Before
    fun setup() {
        mockLanguageProvider = mockk(relaxed = true)
        every { mockLanguageProvider.provide() } returns LANGUAGE

        mockTimezoneProvider = mockk(relaxed = true)
        every { mockTimezoneProvider.provide() } returns TIMEZONE

        mockApplicationVersionProvider = mockk(relaxed = true)
        every { mockApplicationVersionProvider.provide() } returns APP_VERSION

        mockHardwareIdProvider = mockk(relaxed = true)
        every { mockHardwareIdProvider.provide() } returns HW_ID

        deviceInfoCollector = DeviceInfoCollector(
            mockTimezoneProvider,
            mockLanguageProvider,
            mockApplicationVersionProvider,
            true,
            mockHardwareIdProvider,
            json
        )
    }

    @Test
    fun getHardwareId_shouldReturnGenerateNewId_ifStorageReturnsNull() {
        deviceInfoCollector.getHardwareId() shouldBe HW_ID
    }

    @Test
    fun collect_shouldReturn_deviceInfo() {
        val expectedDeviceInfo = DeviceInfo(
            platform = "android",
            applicationVersion = APP_VERSION,
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            languageCode = LANGUAGE,
            timezone = TIMEZONE
        )

        val result = deviceInfoCollector.collect()

        result shouldBe Json.encodeToString(expectedDeviceInfo)
        verify { mockLanguageProvider.provide() }
    }

    @Test
    fun collect_platformShouldBe_huawei() {
        val deviceInfoCollector = DeviceInfoCollector(
            mockTimezoneProvider,
            mockLanguageProvider,
            mockApplicationVersionProvider,
            false,
            mockHardwareIdProvider,
            json
        )

        val result = deviceInfoCollector.collect()

        val deviceInfo = Json.decodeFromString<DeviceInfo>(result)

        deviceInfo.platform shouldBe "android-huawei"
    }
}