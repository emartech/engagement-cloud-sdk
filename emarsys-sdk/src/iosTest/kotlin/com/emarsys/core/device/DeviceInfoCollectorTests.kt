package com.emarsys.core.device

import com.emarsys.KotlinPlatform
import com.emarsys.SdkConstants
import com.emarsys.config.SdkConfig
import com.emarsys.context.SdkContextApi
import com.emarsys.core.device.notification.IosNotificationSettings
import com.emarsys.core.device.notification.IosNotificationSettingsCollectorApi
import com.emarsys.core.providers.ApplicationVersionProviderApi
import com.emarsys.core.providers.LanguageProviderApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.providers.TimezoneProviderApi
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.wrapper.WrapperInfo
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeviceInfoCollectorTests {
    private companion object {
        const val LANGUAGE = "en-US"
        const val APP_VERSION = "2.0"
        const val CLIENT_ID = "test uuid"
        const val DEVICE_MODEL = "iPhone16"
        const val OS_VERSION = "testOsVersion"
        const val TIMEZONE = "+0300"
    }

    private lateinit var mockClientIdProvider: Provider<String>
    private lateinit var mockApplicationVersionProvider: ApplicationVersionProviderApi
    private lateinit var mockLanguageProvider: LanguageProviderApi
    private lateinit var mockTimezoneProvider: TimezoneProviderApi
    private lateinit var mockDeviceInformation: UIDeviceApi
    private lateinit var mockWrapperStorage: TypedStorageApi
    private lateinit var json: Json
    private lateinit var mockStringStorage: StringStorageApi
    private lateinit var mockSdkContext: SdkContextApi
    private lateinit var deviceInfoCollector: DeviceInfoCollector
    private lateinit var mockIosNotificationSettingsCollector: IosNotificationSettingsCollectorApi

    @BeforeTest
    fun setUp() {
        mockClientIdProvider = mock()
        every { mockClientIdProvider.provide() } returns CLIENT_ID
        mockApplicationVersionProvider = mock()
        every { mockApplicationVersionProvider.provide() } returns APP_VERSION
        mockLanguageProvider = mock()
        every { mockLanguageProvider.provide() } returns LANGUAGE
        mockWrapperStorage = mock()
        everySuspend {
            mockWrapperStorage.get(
                StorageConstants.WRAPPER_INFO_KEY,
                WrapperInfo.serializer()
            )
        } returns null
        mockTimezoneProvider = mock()
        every { mockTimezoneProvider.provide() } returns TIMEZONE
        mockDeviceInformation = mock()
        every { mockDeviceInformation.osVersion() } returns OS_VERSION
        every { mockDeviceInformation.deviceModel() } returns DEVICE_MODEL
        json = JsonUtil.json
        mockStringStorage = mock()
        every { mockStringStorage.get(any()) } returns null
        mockSdkContext = mock()
        val mockConfig: SdkConfig = mock()
        every { mockSdkContext.config } returns mockConfig
        every { mockConfig.applicationCode } returns "testAppCode"
        mockIosNotificationSettingsCollector = mock()
        deviceInfoCollector = DeviceInfoCollector(
            mockClientIdProvider,
            mockApplicationVersionProvider,
            mockLanguageProvider,
            mockTimezoneProvider,
            mockDeviceInformation,
            mockWrapperStorage,
            mockIosNotificationSettingsCollector,
            json,
            mockStringStorage,
            mockSdkContext
        )
    }

    @Test
    fun collect_shouldCollectDeviceInfo() = runTest {
        val deviceInfo = DeviceInfo(
            KotlinPlatform.IOS.name.lowercase(),
            SdkConstants.MOBILE_PLATFORM_CATEGORY,
            null,
            null,
            APP_VERSION,
            DEVICE_MODEL,
            OS_VERSION,
            BuildConfig.VERSION_NAME,
            LANGUAGE,
            TIMEZONE,
            CLIENT_ID
        )
        val expected = json.encodeToString(deviceInfo)

        deviceInfoCollector.collect() shouldBe expected
    }

    @Test
    fun getClientId_shouldReturnClientId_fromProvider() = runTest {
        deviceInfoCollector.getClientId() shouldBe CLIENT_ID
    }

    @Test
    fun collect_shouldReturnWithOverriddenLanguage() = runTest {
        every { mockStringStorage.get(SdkConstants.LANGUAGE_STORAGE_KEY) } returns "hu-HU"

        val deviceInfo = DeviceInfo(
            KotlinPlatform.IOS.name.lowercase(),
            SdkConstants.MOBILE_PLATFORM_CATEGORY,
            null,
            null,
            APP_VERSION,
            DEVICE_MODEL,
            OS_VERSION,
            BuildConfig.VERSION_NAME,
            "hu-HU",
            TIMEZONE,
            CLIENT_ID
        )
        val expected = json.encodeToString(deviceInfo)

        deviceInfoCollector.collect() shouldBe expected
    }

    @Test
    fun collectAsDeviceInfoForLogs_shouldReturnDeviceInfo() = runTest {
        val expectedDeviceInfo = DeviceInfoForLogs(
            platform = "ios",
            platformCategory = "mobile",
            platformWrapper = null,
            platformWrapperVersion = null,
            applicationVersion = APP_VERSION,
            deviceModel = DEVICE_MODEL,
            osVersion = OS_VERSION,
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

    @Test
    fun getNotificationSettings_shouldReturn_enabledTrue_whenAuthorizationStatusIsAuthorized() =
        runTest {
            forAll(
                row(IosAuthorizationStatus.Authorized, true),
                row(IosAuthorizationStatus.NotDetermined, false),
                row(IosAuthorizationStatus.Ephemeral, false),
                row(IosAuthorizationStatus.Provisional, false),
                row(IosAuthorizationStatus.Denied, false),
            ) { authorizationStatus, expectedAreNotificationsEnabled ->
                val testIosNotificationSettings =
                    getTestIosNotificationSettings().copy(authorizationStatus = authorizationStatus)
                everySuspend {
                    mockIosNotificationSettingsCollector.collect()
                } returns testIosNotificationSettings

                val result = deviceInfoCollector.getNotificationSettings()

                result shouldBe NotificationSettings(expectedAreNotificationsEnabled)
            }
        }

    @Test
    fun getPlatformCategory_shouldReturn_mobilePlatformCategory() {
        deviceInfoCollector.getPlatformCategory() shouldBe SdkConstants.MOBILE_PLATFORM_CATEGORY
    }

    private fun getTestIosNotificationSettings() = IosNotificationSettings(
        authorizationStatus = IosAuthorizationStatus.Authorized,
        soundSetting = IosNotificationSetting.Enabled,
        badgeSetting = IosNotificationSetting.Enabled,
        alertSetting = IosNotificationSetting.Enabled,
        notificationCenterSetting = IosNotificationSetting.Enabled,
        lockScreenSetting = IosNotificationSetting.Enabled,
        carPlaySetting = IosNotificationSetting.Disabled,
        alertStyle = IosAlertStyle.Banner,
        showPreviewsSetting = IosShowPreviewSetting.Always,
        criticalAlertSetting = IosNotificationSetting.Disabled,
        providesAppNotificationSettings = false,
        scheduledDeliverySetting = IosNotificationSetting.Disabled,
        timeSensitiveSetting = IosNotificationSetting.Disabled
    )
}