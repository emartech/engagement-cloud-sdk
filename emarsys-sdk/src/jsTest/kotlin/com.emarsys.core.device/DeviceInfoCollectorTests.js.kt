package com.emarsys.core.device

import com.emarsys.core.device.fakes.FakeStorage
import com.emarsys.core.device.fakes.FakeUuidProvider
import com.emarsys.core.device.fakes.FakeWebPlatformInfoCollector
import io.kotest.matchers.shouldBe
import kotlinx.browser.window
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class DeviceInfoCollectorTests {

    private companion object {
        const val TEST_UUID = "test uuid"
        const val STORED_ID = "stored hardware id"
    }

    private lateinit var fakeWebPlatformInfoCollector: PlatformInfoCollectorApi
    private lateinit var fakeStorage: FakeStorage
    private lateinit var fakeUuidProvider: FakeUuidProvider
    private lateinit var deviceInfoCollector: DeviceInfoCollector

    @Test
    fun collect_shouldReturn_deviceInfo() {
        val navigator = window.navigator
        val expectedPlatformInfo = WebPlatformInfo(
            null,
            false,
            osName = "Macintosh",
            osVersion = "OS X 11.0.5",
            browserName = "Chrome",
            browserVersion = "160.5.12"
        )
        val expectedDeviceInfo = DeviceInformation(
            platform = navigator.platform,
            manufacturer = navigator.vendor,
            displayMetrics = "${window.innerWidth}x${window.innerHeight}",
            model = navigator.product,
            sdkVersion = BuildConfig.VERSION_NAME,
            language = navigator.language,
            timezone = Clock.System.now().offsetIn(TimeZone.currentSystemDefault()).toString(),
            hardwareId = TEST_UUID,
            platformInfo = Json.encodeToString(expectedPlatformInfo),
            applicationVersion = UNKNOWN_VERSION_NAME
        )

        fakeStorage = FakeStorage()
        fakeUuidProvider = FakeUuidProvider(TEST_UUID)
        val onCollectCalled: () -> String = { Json.encodeToString(expectedPlatformInfo) }
        fakeWebPlatformInfoCollector = FakeWebPlatformInfoCollector(onCollectCalled)
        deviceInfoCollector =
            DeviceInfoCollector(fakeWebPlatformInfoCollector, fakeUuidProvider, fakeStorage)

        val result = deviceInfoCollector.collect()

        result shouldBe Json.encodeToString(expectedDeviceInfo)
    }

    @Test
    fun getHardwareId_shouldGenerateNewId_ifStoredId_isNull() {
        fakeStorage = FakeStorage()
        fakeUuidProvider = FakeUuidProvider(TEST_UUID)
        fakeWebPlatformInfoCollector = FakeWebPlatformInfoCollector()
        deviceInfoCollector =
            DeviceInfoCollector(fakeWebPlatformInfoCollector, fakeUuidProvider, fakeStorage)

        val result = deviceInfoCollector.collect()

        Json.decodeFromString<DeviceInformation>(result).hardwareId shouldBe TEST_UUID
    }

    @Test
    fun getHardwareId_shouldReturnStoredId_ifPresent() {
        fakeStorage = FakeStorage()
        fakeStorage.put("hardwareId", STORED_ID)
        fakeUuidProvider = FakeUuidProvider(TEST_UUID)
        fakeWebPlatformInfoCollector = FakeWebPlatformInfoCollector()
        deviceInfoCollector =
            DeviceInfoCollector(fakeWebPlatformInfoCollector, fakeUuidProvider, fakeStorage)

        val result = deviceInfoCollector.collect()

        Json.decodeFromString<DeviceInformation>(result).hardwareId shouldBe STORED_ID
    }
}