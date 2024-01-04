package com.emarsys.core.device

import com.emarsys.core.device.fakes.FakeWebDeviceInfoCollector
import io.kotest.matchers.shouldBe
import kotlinx.browser.window
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class DeviceInfoCollectorTests {

    private lateinit var fakeWebDeviceInfoCollector: DeviceInfoCollectorApi
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
            hardwareId = "test hwid",
            platformInfo = Json.encodeToString(expectedPlatformInfo)
        )

        val onCollectCalled: () -> String = { Json.encodeToString(expectedPlatformInfo) }
        fakeWebDeviceInfoCollector = FakeWebDeviceInfoCollector(onCollectCalled)
        deviceInfoCollector = DeviceInfoCollector(fakeWebDeviceInfoCollector)

        val result = deviceInfoCollector.collect()

        result shouldBe Json.encodeToString(expectedDeviceInfo)
    }
}