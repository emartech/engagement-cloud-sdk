package com.sap.ec.core.device

import dev.mokkery.mock
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DeviceCategoryProviderTests {

    @Test
    fun getDeviceCategory_shouldReturn_correctDeviceCategory() = runTest {
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
            val provider = DeviceCategoryProvider(userAgent, null, sdkLogger = mock())

            provider.getDeviceCategory() shouldBe deviceCategory
        }
    }

    @Test
    fun getDeviceCategory_shouldReturn_unknown_whenUserAgentCannotBeParsed() = runTest {
        val provider = DeviceCategoryProvider("UNKNOWN_USER_AGENT", null, sdkLogger = mock())

        provider.getDeviceCategory() shouldBe DeviceCategory.UNKNOWN
    }
}
