package com.emarsys.core.device

import android.os.Build
import com.emarsys.SdkConstants
import com.emarsys.context.SdkContextApi
import com.emarsys.core.device.notification.AndroidNotificationSettings
import com.emarsys.core.device.notification.AndroidNotificationSettingsCollectorApi
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.providers.TimezoneProviderApi
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.wrapper.WrapperInfo
import com.emarsys.util.JsonUtil
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
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

    private lateinit var mockLanguageProvider: LanguageProviderApi
    private lateinit var mockTimezoneProvider: TimezoneProviderApi
    private lateinit var mockApplicationVersionProvider: ApplicationVersionProviderApi
    private lateinit var mockClientIdProvider: Provider<String>
    private lateinit var mockPlatformInfoCollector: PlatformInfoCollector
    private lateinit var mockStorage: TypedStorageApi
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var deviceInfoCollector: DeviceInfoCollector
    private lateinit var mockAndroidNotificationSettingsCollector: AndroidNotificationSettingsCollectorApi
    private lateinit var mockSdkContext: SdkContextApi
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
        coEvery { mockClientIdProvider.provide() } returns CLIENT_ID

        mockPlatformInfoCollector = mockk(relaxed = true)

        mockStorage = mockk(relaxed = true)
        coEvery { mockStorage.get(any<String>(), WrapperInfo.serializer()) } returns null

        mockStringStorage = mockk(relaxed = true)
        every { mockStringStorage.get(any()) } returns null

        mockSdkContext = mockk(relaxed = true)
        every { mockSdkContext.config?.applicationCode } returns "testAppCode"

        mockAndroidNotificationSettingsCollector = mockk(relaxed = true)

        deviceInfoCollector = DeviceInfoCollector(
            mockTimezoneProvider,
            mockLanguageProvider,
            mockApplicationVersionProvider,
            true,
            mockClientIdProvider,
            mockPlatformInfoCollector,
            mockStorage,
            mockAndroidNotificationSettingsCollector,
            json,
            mockStringStorage,
            mockSdkContext
        )
    }

    @Test
    fun getClientId_shouldReturnGenerateNewId_ifStorageReturnsNull() = runTest {
        deviceInfoCollector.getClientId() shouldBe CLIENT_ID
    }

    @Test
    fun collect_shouldReturn_deviceInfo_whenNative() = runTest {
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
    fun collect_shouldReturn_deviceInfo_whenWrapper() = runTest {
        val expectedWrapperInfo = WrapperInfo(WRAPPER_PLATFORM, WRAPPER_VERSION)
        coEvery {
            mockStorage.get(
                StorageConstants.WRAPPER_INFO_KEY,
                WrapperInfo.serializer()
            )
        } returns expectedWrapperInfo

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
    fun collect_platformShouldBe_huawei() = runTest {
        val deviceInfoCollector = DeviceInfoCollector(
            mockTimezoneProvider,
            mockLanguageProvider,
            mockApplicationVersionProvider,
            false,
            mockClientIdProvider,
            mockPlatformInfoCollector,
            mockStorage,
            mockAndroidNotificationSettingsCollector,
            json,
            mockStringStorage,
            mockSdkContext
        )

        val result = deviceInfoCollector.collect()

        val deviceInfo = json.decodeFromString<DeviceInfo>(result)

        deviceInfo.platform shouldBe "android-huawei"
    }

    @Test
    fun getPushSettings_shouldCall_androidNotificationSettingsCollector_andReturn_enabledTrue() = runTest {
        val testSettings = AndroidNotificationSettings(
            true, -1000, listOf(ChannelSettings("testChannelId"))
        )
        val expectedNotificationSettings = NotificationSettings(true)

        every { mockAndroidNotificationSettingsCollector.collect() } returns testSettings

        val result = deviceInfoCollector.getNotificationSettings()

        result shouldBe expectedNotificationSettings
    }

    @Test
    fun getPushSettings_shouldCall_androidNotificationSettingsCollector_andReturn_enabledFalse() = runTest {
        val testSettings = AndroidNotificationSettings(
            false, -1000, listOf(ChannelSettings("testChannelId"))
        )
        val expectedNotificationSettings = NotificationSettings(false)

        every { mockAndroidNotificationSettingsCollector.collect() } returns testSettings

        val result = deviceInfoCollector.getNotificationSettings()

        result shouldBe expectedNotificationSettings
    }

    @Test
    fun collect_shouldReturn_overWritten_language() = runTest {
        every {
            mockStringStorage.get(SdkConstants.LANGUAGE_STORAGE_KEY)
        } returns "hu-HU"


        val expectedDeviceInfo = DeviceInfo(
            platform = "android",
            platformCategory = "mobile",
            platformWrapper = null,
            platformWrapperVersion = null,
            applicationVersion = APP_VERSION,
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            language = "hu-HU",
            timezone = TIMEZONE,
            clientId = CLIENT_ID
        )

        val result = deviceInfoCollector.collect()

        result shouldBe json.encodeToString(expectedDeviceInfo)
    }

    @Test
    fun collectAsDeviceInfoForLogs_shouldReturn_deviceInfo_whenNative() = runTest {
        val expectedDeviceInfo = DeviceInfoForLogs(
            platform = "android",
            platformCategory = "mobile",
            platformWrapper = null,
            platformWrapperVersion = null,
            applicationVersion = APP_VERSION,
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            isDebugMode = true,
            applicationCode = "testAppCode",
            language = LANGUAGE,
            timezone = TIMEZONE,
            clientId = CLIENT_ID
        )

        val result = deviceInfoCollector.collectAsDeviceInfoForLogs()

        result shouldBe expectedDeviceInfo
    }
}