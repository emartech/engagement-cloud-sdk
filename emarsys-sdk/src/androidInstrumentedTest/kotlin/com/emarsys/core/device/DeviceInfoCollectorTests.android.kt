package com.emarsys.core.device

import android.content.res.Resources
import android.os.Build
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.providers.Provider
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

class DeviceInfoCollectorTests {

    private companion object {
        private const val LANGUAGE = "en-US"
        private const val APP_VERSION = "2.0"
        private const val GENERATED_ID = "test uuid"
        private const val STORED_ID = "stored hardware id"
        private const val PLATFORM_INFO = "testPlatformInfo"
    }

    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockUuidProvider: Provider<String>
    private lateinit var mockStorage: TypedStorageApi<String?>
    private lateinit var timeZone: TimeZone
    private lateinit var mockAndroidPlatformInfoCollector: AndroidPlatformInfoCollector

    @Before
    fun setup() {
        timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        TimeZone.setDefault(timeZone)

        mockLanguageProvider = mockk(relaxed = true)
        every { mockLanguageProvider.provideLanguage() } returns LANGUAGE

        mockStorage = mockk(relaxed = true)
        every { mockStorage.get("hardwareId") } returns null

        mockUuidProvider = mockk(relaxed = true)
        every { mockUuidProvider.provide() } returns GENERATED_ID

        mockAndroidPlatformInfoCollector = mockk(relaxed = true)
        every { mockAndroidPlatformInfoCollector.collect() } returns PLATFORM_INFO
    }

    @After
    fun teardown() {
        TimeZone.setDefault(null)
    }

    @Test
    fun getHardwareId_shouldReturnGenerateNewId_ifStorageReturnsNull() {
        val deviceInfoCollector =
            DeviceInfoCollector(
                mockAndroidPlatformInfoCollector,
                mockLanguageProvider,
                mockUuidProvider,
                mockStorage,
                true,
            )

        deviceInfoCollector.getHardwareId() shouldBe GENERATED_ID
    }

    @Test
    fun getHardwareId_shouldReturnStoreValue_ifPresent() {
        every { mockStorage.get("hardwareId") } returns STORED_ID

        val deviceInfoCollector =
            DeviceInfoCollector(
                mockAndroidPlatformInfoCollector,
                mockLanguageProvider,
                mockUuidProvider,
                mockStorage,
                true,
            )

        deviceInfoCollector.getHardwareId() shouldBe STORED_ID
    }

    @Test
    fun collect_shouldReturn_deviceInfo() {
        every { mockAndroidPlatformInfoCollector.applicationVersion() } returns APP_VERSION

        val expectedDeviceInfo = DeviceInformation(
            platform = "android",
            manufacturer = Build.MANUFACTURER,
            displayMetrics = "${Resources.getSystem().displayMetrics.widthPixels}x${Resources.getSystem().displayMetrics.heightPixels}",
            model = Build.MODEL,
            sdkVersion = BuildConfig.VERSION_NAME,
            language = LANGUAGE,
            timezone = "+0900",
            hardwareId = GENERATED_ID,
            platformInfo = PLATFORM_INFO,
            applicationVersion = APP_VERSION
        )
        val deviceInfoCollector =
            DeviceInfoCollector(
                mockAndroidPlatformInfoCollector,
                mockLanguageProvider,
                mockUuidProvider,
                mockStorage,
                true,
            )

        val result = deviceInfoCollector.collect()

        result shouldBe Json.encodeToString(expectedDeviceInfo)
        verify { mockLanguageProvider.provideLanguage() }
    }

    @Test
    fun collect_platformShouldBe_huawei() {
        val deviceInfoCollector =
            DeviceInfoCollector(
                mockAndroidPlatformInfoCollector,
                mockLanguageProvider,
                mockUuidProvider,
                mockStorage,
                false,
            )

        val result = deviceInfoCollector.collect()

        val deviceInfo = Json.decodeFromString<DeviceInformation>(result)

        deviceInfo.platform shouldBe "android-huawei"
    }
}