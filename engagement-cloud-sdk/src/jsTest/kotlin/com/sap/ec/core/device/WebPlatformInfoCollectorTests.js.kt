package com.sap.ec.core.device

import com.sap.ec.core.device.constants.BrowserInfo
import com.sap.ec.core.device.constants.OsInfo
import dev.mokkery.mock
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class WebPlatformInfoCollectorTests {
    private companion object {
        const val EXTRA = "some extra text, so the header-gets-bigger"
        const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
    }

    @Test
    fun collect_shouldReturn_correctOsNameAndVersion() = runTest {
        OsInfo.entries.forEach {
            val testNavigatorData =
                """${it.value} $EXTRA ${it.versionPrefix} 1.2.3 $EXTRA Chrome Chrome 3.4.5"""
            val webPlatformInfoCollector =
                WebPlatformInfoCollector(
                    testNavigatorData,
                    userAgent = USER_AGENT,
                    null,
                    sdkLogger = mock()
                )
            val expectedPlatformInfo = WebPlatformInfo(
                null,
                false,
                it.name,
                "1.2.3",
                "chrome",
                "3.4.5",
                DeviceCategory.DESKTOP
            )

            val result = webPlatformInfoCollector.collect()

            result shouldBe expectedPlatformInfo
        }
    }

    @Test
    fun collect_shouldReturn_correctBrowserNameAndVersion() = runTest {
        BrowserInfo.entries.forEach {
            val testNavigatorData =
                """Android Android 1.2.3 ${it.value} $EXTRA ${it.versionPrefix} 5.6.7 $EXTRA"""
            val webPlatformInfoCollector =
                WebPlatformInfoCollector(
                    testNavigatorData,
                    userAgent = USER_AGENT,
                    null,
                    sdkLogger = mock()
                )
            val expectedPlatformInfo = WebPlatformInfo(
                null,
                false,
                OsInfo.Android.name,
                "1.2.3",
                it.name.lowercase(),
                "5.6.7",
                DeviceCategory.DESKTOP
            )

            val result = webPlatformInfoCollector.collect()

            result shouldBe expectedPlatformInfo
        }
    }

    @Test
    fun collect_shouldReturn_unknown_ifNoBrowserMatchWasFound() = runTest {
        val navigatorDataWithoutMatches =
            "this should not occur ${OsInfo.IPhone.value} $EXTRA ${OsInfo.IPhone.versionPrefix} 6.5.4"
        val webPlatformInfoCollector =
            WebPlatformInfoCollector(
                navigatorDataWithoutMatches,
                userAgent = USER_AGENT,
                null,
                sdkLogger = mock()
            )
        val expectation = WebPlatformInfo(
            null,
            false,
            OsInfo.IPhone.name,
            "6.5.4",
            "unknown",
            "0",
            DeviceCategory.DESKTOP
        )

        val result = webPlatformInfoCollector.collect()

        result shouldBe expectation
    }

    @Test
    fun collect_shouldReturn_unknown_ifNoOSMatchWasFound() = runTest {
        val navigatorDataWithoutMatches =
            "this should not occur ${BrowserInfo.Chrome.value} $EXTRA ${BrowserInfo.Chrome.versionPrefix} 9.8.7"
        val webPlatformInfoCollector =
            WebPlatformInfoCollector(
                navigatorDataWithoutMatches,
                userAgent = USER_AGENT,
                null,
                sdkLogger = mock()
            )
        val expectation = WebPlatformInfo(
            null,
            false,
            OsInfo.Unknown.name,
            "0",
            "chrome",
            "9.8.7",
            DeviceCategory.DESKTOP
        )

        val result = webPlatformInfoCollector.collect()

        result shouldBe expectation
    }

    @Test
    fun collect_shouldReturn_correctDeviceCategory() = runTest {
        forAll(
            table(
                headers("userAgent", "deviceCategory"),
                listOf(
                    // mobile
                    row(
                        "Mozilla/5.0 (Linux; Android 8.0; Pixel XL Build/OPP3.170518.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.0 Mobile Safari/537.36 EdgA/41.1.35.1",
                        DeviceCategory.MOBILE
                    ),
                    row(
                        "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53",
                        DeviceCategory.MOBILE
                    ),
                    // desktop
                    row(
                        "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.17 Safari/537.36",
                        DeviceCategory.DESKTOP
                    ),
                    row(
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.3 Safari/605.1.15",
                        DeviceCategory.DESKTOP
                    ),
                    // tablet
                    row(
                        "Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.1.0; en-US) AppleWebKit/536.2+ (KHTML, like Gecko) Version/7.2.1.0 Safari/536.2+",
                        DeviceCategory.TABLET
                    ),
                    // tv
                    row(
                        "Mozilla/5.0 (SMART-TV; X11; Linux armv7l) AppleWebKit/537.42 (KHTML, like Gecko) Chromium/25.0.1349.2 Chrome/25.0.1349.2 Safari/537.42",
                        DeviceCategory.TV
                    )
                )
            )
        ) { userAgent, deviceCategory ->
            val webPlatformInfoCollector =
                WebPlatformInfoCollector("testNavigatorData", userAgent, null, sdkLogger = mock())
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
    }

    @Test
    fun collect_shouldReturn_unknownDeviceCategory_when() = runTest {
        val result =
            WebPlatformInfoCollector(
                "testNavigatorData",
                "UNKNOWN_USER_AGENT",
                null,
                sdkLogger = mock()
            ).collect()
        result.deviceCategory shouldBe DeviceCategory.UNKNOWN
    }
}