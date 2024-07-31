package com.emarsys.core.device

import android.os.Build
import com.emarsys.core.providers.Provider
import com.emarsys.util.JsonUtil
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
    private lateinit var mockPlatformInfoCollector: PlatformInfoCollector
    private lateinit var deviceInfoCollector: DeviceInfoCollector
    private val json = JsonUtil.json

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

        mockPlatformInfoCollector = mockk(relaxed = true)

        deviceInfoCollector = DeviceInfoCollector(
            mockTimezoneProvider,
            mockLanguageProvider,
            mockApplicationVersionProvider,
            true,
            mockHardwareIdProvider,
            mockPlatformInfoCollector,
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
            mockPlatformInfoCollector,
            json
        )

        val result = deviceInfoCollector.collect()

        val deviceInfo = json.decodeFromString<DeviceInfo>(result)

        deviceInfo.platform shouldBe "android-huawei"
    }

    @Test
    fun getPushSettings_shouldCall_getPushSettings_onPlatformInfoCollector() {
        val testSettings = AndroidNotificationSettings(
            true, -1000, listOf(ChannelSettings("testChannelId"))
        )

        every { mockPlatformInfoCollector.notificationSettings() } returns testSettings

        val result = deviceInfoCollector.getPushSettings()

        result shouldBe testSettings
    }
}