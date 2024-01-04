package com.emarsys.core.device

import com.emarsys.core.device.constants.BrowserInfo
import com.emarsys.core.device.constants.OsInfo
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.test.Test

class WebDeviceInfoCollectorTests {
    private companion object {
        const val EXTRA = "some extra text, so the header-gets-bigger"
    }

    @Test
    fun collect_shouldReturn_correctOsNameAndVersion() {
        OsInfo.entries.forEach {
            val testNavigatorData =
                """${it.value} $EXTRA ${it.versionPrefix} 1.2.3 $EXTRA Chrome Chrome 3.4.5"""
            val webDeviceInfoCollector = WebDeviceInfoCollector(testNavigatorData)
            val expectedPlatformInfo = WebPlatformInfo(
                null,
                false,
                it.name,
                "1.2.3",
                BrowserInfo.Chrome.name,
                "3.4.5"
            )

            val result = webDeviceInfoCollector.collect()

            val webPlatformInfo = Json.decodeFromString<WebPlatformInfo>(result)

            webPlatformInfo shouldBe expectedPlatformInfo
        }
    }

    @Test
    fun collect_shouldReturn_correctBrowserNameAndVersion() {
        BrowserInfo.entries.forEach {
            val testNavigatorData =
                """Android Android 1.2.3 ${it.value} $EXTRA ${it.versionPrefix} 5.6.7 $EXTRA"""
            val webDeviceInfoCollector = WebDeviceInfoCollector(testNavigatorData)
            val expectedPlatformInfo = WebPlatformInfo(
                null,
                false,
                OsInfo.Android.name,
                "1.2.3",
                it.name,
                "5.6.7"
            )

            val result = webDeviceInfoCollector.collect()

            val webPlatformInfo = Json.decodeFromString<WebPlatformInfo>(result)

            webPlatformInfo shouldBe expectedPlatformInfo
        }
    }

    @Test
    fun collect_shouldReturn_unknown_ifNoBrowserMatchWasFound() {
        val navigatorDataWithoutMatches = "this should not occur ${OsInfo.IPhone.value} $EXTRA ${OsInfo.IPhone.versionPrefix} 6.5.4"
        val webDeviceInfoCollector = WebDeviceInfoCollector(navigatorDataWithoutMatches)
        val expectation = WebPlatformInfo(
            null,
            false,
            OsInfo.IPhone.name,
            "6.5.4",
            BrowserInfo.Unknown.name,
            "0"
        )

        val result = webDeviceInfoCollector.collect()

        val platformInfo = Json.decodeFromString<WebPlatformInfo>(result)

        platformInfo shouldBe expectation
    }

    @Test
    fun collect_shouldReturn_unknown_ifNoOSMatchWasFound() {
        val navigatorDataWithoutMatches = "this should not occur ${BrowserInfo.Chrome.value} $EXTRA ${BrowserInfo.Chrome.versionPrefix} 9.8.7"
        val webDeviceInfoCollector = WebDeviceInfoCollector(navigatorDataWithoutMatches)
        val expectation = WebPlatformInfo(
            null,
            false,
            OsInfo.Unknown.name,
            "0",
            BrowserInfo.Chrome.name,
            "9.8.7"
        )

        val result = webDeviceInfoCollector.collect()

        val platformInfo = Json.decodeFromString<WebPlatformInfo>(result)

        platformInfo shouldBe expectation
    }
}