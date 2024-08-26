package com.emarsys.core.device

import com.emarsys.KotlinPlatform
import com.emarsys.core.providers.Provider
import com.emarsys.util.JsonUtil
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

class DeviceInfoCollectorTests {
    private companion object {
        const val LANGUAGE = "en-US"
        const val APP_VERSION = "2.0"
        const val HW_ID = "test uuid"
        const val DEVICE_MODEL = "iPhone16"
        const val OS_VERSION = "testOsVersion"
        const val TIMEZONE = "+0300"
    }

    private lateinit var mockHardwareIdProvider: Provider<String>
    private lateinit var mockApplicationVersionProvider: Provider<String>
    private lateinit var mockLanguageProvider: Provider<String>
    private lateinit var mockTimezoneProvider: Provider<String>
    private lateinit var mockDeviceInformation: UIDeviceApi
    private lateinit var json: Json

    private lateinit var deviceInfoCollector: DeviceInfoCollector

    @BeforeTest
    fun setUp() {
        mockHardwareIdProvider = mock()
        every { mockHardwareIdProvider.provide() } returns HW_ID
        mockApplicationVersionProvider = mock()
        every { mockApplicationVersionProvider.provide() } returns APP_VERSION
        mockLanguageProvider = mock()
        every { mockLanguageProvider.provide() } returns LANGUAGE
        mockTimezoneProvider = mock()
        every { mockTimezoneProvider.provide() } returns TIMEZONE
        mockDeviceInformation = mock()
        every { mockDeviceInformation.osVersion() } returns OS_VERSION
        every { mockDeviceInformation.deviceModel() } returns DEVICE_MODEL
        json = JsonUtil.json

        deviceInfoCollector = DeviceInfoCollector(
            mockHardwareIdProvider,
            mockApplicationVersionProvider,
            mockLanguageProvider,
            mockTimezoneProvider,
            mockDeviceInformation,
            json
        )
    }

    @Test
    fun collect_shouldCollectDeviceInfo() {
        val deviceInfo = DeviceInfo(
            KotlinPlatform.IOS.name.lowercase(),
            APP_VERSION,
            DEVICE_MODEL,
            OS_VERSION,
            BuildConfig.VERSION_NAME,
            LANGUAGE,
            TIMEZONE
        )
        val expected = json.encodeToString(deviceInfo)

        deviceInfoCollector.collect() shouldBe expected
    }

    @Test
    fun getHardwareId_shouldReturnHardwareId_fromProvider() {
        deviceInfoCollector.getHardwareId() shouldBe HW_ID
    }
}