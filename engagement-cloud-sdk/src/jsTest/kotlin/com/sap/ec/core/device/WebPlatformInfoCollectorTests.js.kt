package com.sap.ec.core.device

import com.sap.ec.SdkConstants
import com.sap.ec.core.device.constants.BrowserInfo
import com.sap.ec.core.device.constants.OsInfo
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import kotlinx.browser.window
import kotlin.test.Test

class WebPlatformInfoCollectorTests {
    private companion object {
        const val EXTRA = "some extra text, so the header-gets-bigger"
    }

    @Test
    fun collect_shouldReturn_correctOsNameAndVersion() {
        OsInfo.entries.forEach {
            val testNavigatorData =
                """${it.value} $EXTRA ${it.versionPrefix} 1.2.3 $EXTRA Chrome Chrome 3.4.5"""
            val webPlatformInfoCollector = WebPlatformInfoCollector(testNavigatorData)
            val expectedPlatformInfo = WebPlatformInfo(
                null,
                false,
                it.name,
                "1.2.3",
                "chrome",
                "3.4.5",
                SdkConstants.DESKTOP_DEVICE_CATEGORY
            )

            val result = webPlatformInfoCollector.collect()

            result shouldBe expectedPlatformInfo
        }
    }

    @Test
    fun collect_shouldReturn_correctBrowserNameAndVersion() {
        BrowserInfo.entries.forEach {
            val testNavigatorData =
                """Android Android 1.2.3 ${it.value} $EXTRA ${it.versionPrefix} 5.6.7 $EXTRA"""
            val webPlatformInfoCollector = WebPlatformInfoCollector(testNavigatorData)
            val expectedPlatformInfo = WebPlatformInfo(
                null,
                false,
                OsInfo.Android.name,
                "1.2.3",
                it.name.lowercase(),
                "5.6.7",
                SdkConstants.DESKTOP_DEVICE_CATEGORY
            )

            val result = webPlatformInfoCollector.collect()

            result shouldBe expectedPlatformInfo
        }
    }

    @Test
    fun collect_shouldReturn_unknown_ifNoBrowserMatchWasFound() {
        val navigatorDataWithoutMatches = "this should not occur ${OsInfo.IPhone.value} $EXTRA ${OsInfo.IPhone.versionPrefix} 6.5.4"
        val webPlatformInfoCollector = WebPlatformInfoCollector(navigatorDataWithoutMatches)
        val expectation = WebPlatformInfo(
            null,
            false,
            OsInfo.IPhone.name,
            "6.5.4",
            "unknown",
            "0",
            SdkConstants.DESKTOP_DEVICE_CATEGORY
        )

        val result = webPlatformInfoCollector.collect()

        result shouldBe expectation
    }

    @Test
    fun collect_shouldReturn_unknown_ifNoOSMatchWasFound() {
        val navigatorDataWithoutMatches = "this should not occur ${BrowserInfo.Chrome.value} $EXTRA ${BrowserInfo.Chrome.versionPrefix} 9.8.7"
        val webPlatformInfoCollector = WebPlatformInfoCollector(navigatorDataWithoutMatches)
        val expectation = WebPlatformInfo(
            null,
            false,
            OsInfo.Unknown.name,
            "0",
            "chrome",
            "9.8.7",
            SdkConstants.DESKTOP_DEVICE_CATEGORY
        )

        val result = webPlatformInfoCollector.collect()

        result shouldBe expectation
    }

    @Test
    fun collect_shouldReturn_correctDeviceCategory() {
        forAll(
            table(
                headers("userAgent", "deviceCategory"),
                listOf(
                    // mobile
                    row("Mozilla/5.0 (Linux; Android 10; Pixel 2) AppleWebKit/537.36 (KHTML, like Gecko) Edg/57.0.986.6", SdkConstants.MOBILE_DEVICE_CATEGORY),
                    row("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53", SdkConstants.MOBILE_DEVICE_CATEGORY),
                    // desktop
                    row("Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.17 Safari/537.36", SdkConstants.DESKTOP_DEVICE_CATEGORY),
                    row("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15", SdkConstants.DESKTOP_DEVICE_CATEGORY),
                )
            )
        ) { userAgent, deviceCategory ->
            js("""
                Object.defineProperty(window.navigator, 'userAgent', {
                    value: userAgent,
                    configurable: true,
                    writable: true
                });
            """)
            window.navigator.userAgent shouldBe userAgent

            val webPlatformInfoCollector = WebPlatformInfoCollector("testNavigatorData")
            val result = webPlatformInfoCollector.collect()
            val expectation = WebPlatformInfo(
                null,
                false,
                OsInfo.Unknown.name,
                "0",
                BrowserInfo.Unknown.name.lowercase(),
                "0",
                deviceCategory
            )

            result shouldBe expectation
        }

        js("""
            delete window.navigator.userAgent;
        """)
    }
}