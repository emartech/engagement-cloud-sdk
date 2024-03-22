package com.emarsys.core.device

import android.os.Build
import com.emarsys.core.providers.Provider
import com.emarsys.core.storage.TypedStorageApi
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
        const val GENERATED_ID = "test uuid"
        const val STORED_ID = "stored hardware id"
        const val TIMEZONE = "+0300"
    }

    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var mockUuidProvider: Provider<String>
    private lateinit var mockTimezoneProvider: Provider<String>
    private lateinit var mockStorage: TypedStorageApi<String?>
    private lateinit var mockApplicationVersionProvider: Provider<String>
    private lateinit var deviceInfoCollector: DeviceInfoCollector

    @Before
    fun setup() {
        mockLanguageProvider = mockk(relaxed = true)
        every { mockLanguageProvider.provideLanguage() } returns LANGUAGE

        mockTimezoneProvider = mockk(relaxed = true)
        every { mockTimezoneProvider.provide() } returns TIMEZONE

        mockApplicationVersionProvider = mockk(relaxed = true)
        every { mockApplicationVersionProvider.provide() } returns APP_VERSION

        mockStorage = mockk(relaxed = true)
        every { mockStorage.get("hardwareId") } returns null

        mockUuidProvider = mockk(relaxed = true)
        every { mockUuidProvider.provide() } returns GENERATED_ID

        deviceInfoCollector = DeviceInfoCollector(
            mockUuidProvider,
            mockTimezoneProvider,
            mockLanguageProvider,
            mockApplicationVersionProvider,
            mockStorage,
            true,
        )
    }

    @Test
    fun getHardwareId_shouldReturnGenerateNewId_ifStorageReturnsNull() {
        deviceInfoCollector.getHardwareId() shouldBe GENERATED_ID
    }

    @Test
    fun getHardwareId_shouldReturnStoreValue_ifPresent() {
        every { mockStorage.get("hardwareId") } returns STORED_ID

        deviceInfoCollector.getHardwareId() shouldBe STORED_ID
    }

    @Test
    fun collect_shouldReturn_deviceInfo() {
        val expectedDeviceInfo = DeviceInfo(
            platform = "android",
            applicationVersion = APP_VERSION,
            deviceModel = Build.MODEL,
            osVersion = SdkBuildConfig.getOsVersion(),
            sdkVersion = BuildConfig.VERSION_NAME,
            language = LANGUAGE,
            timezone = TIMEZONE
        )

        val result = deviceInfoCollector.collect()

        result shouldBe Json.encodeToString(expectedDeviceInfo)
        verify { mockLanguageProvider.provideLanguage() }
    }

    @Test
    fun collect_platformShouldBe_huawei() {
        val deviceInfoCollector = DeviceInfoCollector(
            mockUuidProvider,
            mockTimezoneProvider,
            mockLanguageProvider,
            mockApplicationVersionProvider,
            mockStorage,
            false
        )

        val result = deviceInfoCollector.collect()

        val deviceInfo = Json.decodeFromString<DeviceInfo>(result)

        deviceInfo.platform shouldBe "android-huawei"
    }
}