package com.emarsys.core.device

import android.os.Build
import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.wrapper.WrapperInfo
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeviceInfoCollectorTests {

    private companion object {
        const val LANGUAGE = "en-US"
        const val APP_VERSION = "2.0"
        const val CLIENT_ID = "test uuid"
        const val TIMEZONE = "+0300"
        const val WRAPPER_PLATFORM = "flutter"
        const val WRAPPER_VERSION = "1.0.0"
    }

    private lateinit var mockLanguageProvider: Provider<String>
    private lateinit var mockTimezoneProvider: Provider<String>
    private lateinit var mockApplicationVersionProvider: Provider<String>
    private lateinit var mockClientIdProvider: Provider<String>
    private lateinit var mockPlatformInfoCollector: PlatformInfoCollector
    private lateinit var mockStorage: TypedStorageApi<WrapperInfo?>
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

        mockClientIdProvider = mockk(relaxed = true)
        every { mockClientIdProvider.provide() } returns CLIENT_ID

        mockPlatformInfoCollector = mockk(relaxed = true)

        mockStorage = mockk(relaxed = true)
        every { mockStorage.get(any()) } returns null

        deviceInfoCollector = DeviceInfoCollector(
            mockTimezoneProvider,
            mockLanguageProvider,
            mockApplicationVersionProvider,
            true,
            mockClientIdProvider,
            mockPlatformInfoCollector,
            mockStorage,
            json
        )
    }

    @Test
    fun getClientId_shouldReturnGenerateNewId_ifStorageReturnsNull() {
        deviceInfoCollector.getClientId() shouldBe CLIENT_ID
    }

    @Test
    fun collect_shouldReturn_deviceInfo_whenNative() {
        val expectedDeviceInfo = DeviceInfo(
            platform = "android",
            platformCategory = "mobile",
            platformWrapper = null,
            platformWrapperVersion = null,
            applicationVersion = APP_VERSION,
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            language = LANGUAGE,
            timezone = TIMEZONE,
            clientId = CLIENT_ID
        )

        val result = deviceInfoCollector.collect()

        result shouldBe json.encodeToString(expectedDeviceInfo)
        verify { mockLanguageProvider.provide() }
    }

    @Test
    fun collect_shouldReturn_deviceInfo_whenWrapper() {
        val expectedWrapperInfo = WrapperInfo(WRAPPER_PLATFORM, WRAPPER_VERSION)
        every { mockStorage.get(StorageConstants.WRAPPER_INFO_KEY) } returns expectedWrapperInfo

        val expectedDeviceInfo = DeviceInfo(
            platform = "android",
            platformCategory = "mobile",
            platformWrapper = WRAPPER_PLATFORM,
            platformWrapperVersion = WRAPPER_VERSION,
            applicationVersion = APP_VERSION,
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            language = LANGUAGE,
            timezone = TIMEZONE,
            clientId = CLIENT_ID
        )

        val result = deviceInfoCollector.collect()

        result shouldBe json.encodeToString(expectedDeviceInfo)
        verify { mockLanguageProvider.provide() }
    }

    @Test
    fun collect_platformShouldBe_huawei() {
        val deviceInfoCollector = DeviceInfoCollector(
            mockTimezoneProvider,
            mockLanguageProvider,
            mockApplicationVersionProvider,
            false,
            mockClientIdProvider,
            mockPlatformInfoCollector,
            mockStorage,
            json
        )

        val result = deviceInfoCollector.collect()

        val deviceInfo = json.decodeFromString<DeviceInfo>(result)

        deviceInfo.platform shouldBe "android-huawei"
    }

    @Test
    fun getPushSettings_shouldCall_getPushSettings_onPlatformInfoCollector() = runTest {
        val testSettings = AndroidNotificationSettings(
            true, -1000, listOf(ChannelSettings("testChannelId"))
        )

        every { mockPlatformInfoCollector.notificationSettings() } returns testSettings

        val result = deviceInfoCollector.getPushSettings()

        result shouldBe testSettings
    }
}