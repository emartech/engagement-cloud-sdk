package com.emarsys.core.device

import com.emarsys.KotlinPlatform
import com.emarsys.SdkConstants
import com.emarsys.core.providers.Provider
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
    private lateinit var mockApplicationVersionProvider: Provider<String>
    private lateinit var mockLanguageProvider: Provider<String>
    private lateinit var mockTimezoneProvider: Provider<String>
    private lateinit var mockDeviceInformation: UIDeviceApi
    private lateinit var mockWrapperStorage: TypedStorageApi
    private lateinit var json: Json
    private lateinit var mockStringStorage: StringStorageApi

    private lateinit var deviceInfoCollector: DeviceInfoCollector

    @BeforeTest
    fun setUp() {
        mockClientIdProvider = mock()
        every { mockClientIdProvider.provide() } returns CLIENT_ID
        mockApplicationVersionProvider = mock()
        every { mockApplicationVersionProvider.provide() } returns APP_VERSION
        mockLanguageProvider = mock()
        every { mockLanguageProvider.provide() } returns LANGUAGE
        mockWrapperStorage = mock()
        everySuspend { mockWrapperStorage.get(StorageConstants.WRAPPER_INFO_KEY, WrapperInfo.serializer()) } returns null
        mockTimezoneProvider = mock()
        every { mockTimezoneProvider.provide() } returns TIMEZONE
        mockDeviceInformation = mock()
        every { mockDeviceInformation.osVersion() } returns OS_VERSION
        every { mockDeviceInformation.deviceModel() } returns DEVICE_MODEL
        json = JsonUtil.json
        mockStringStorage = mock()
        every { mockStringStorage.get(any()) } returns null

        deviceInfoCollector = DeviceInfoCollector(
            mockClientIdProvider,
            mockApplicationVersionProvider,
            mockLanguageProvider,
            mockTimezoneProvider,
            mockDeviceInformation,
            mockWrapperStorage,
            json,
            mockStringStorage
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
}