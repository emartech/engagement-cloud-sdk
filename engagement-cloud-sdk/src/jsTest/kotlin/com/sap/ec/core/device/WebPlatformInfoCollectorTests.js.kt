package com.sap.ec.core.device

import com.sap.ec.core.device.constants.BrowserInfo
import com.sap.ec.core.device.constants.OsInfo
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class WebPlatformInfoCollectorTests {
    private companion object {
        const val EXTRA = "some extra text, so the header-gets-bigger"
    }

    private fun deviceCategoryProvider(
        deviceCategory: DeviceCategory = DeviceCategory.DESKTOP
    ): DeviceCategoryProviderApi = mock {
        everySuspend { getDeviceCategory() } returns deviceCategory
    }

    @Test
    fun collect_shouldReturn_correctOsNameAndVersion() = runTest {
        OsInfo.entries.forEach {
            val testNavigatorData =
                """${it.value} $EXTRA ${it.versionPrefix} 1.2.3 $EXTRA Chrome Chrome 3.4.5"""
            val webPlatformInfoCollector =
                WebPlatformInfoCollector(testNavigatorData, deviceCategoryProvider())
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
                WebPlatformInfoCollector(testNavigatorData, deviceCategoryProvider())
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
            WebPlatformInfoCollector(navigatorDataWithoutMatches, deviceCategoryProvider())
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
            WebPlatformInfoCollector(navigatorDataWithoutMatches, deviceCategoryProvider())
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
    fun collect_shouldReturn_deviceCategory_fromProvider() = runTest {
        val webPlatformInfoCollector =
            WebPlatformInfoCollector(
                "testNavigatorData",
                deviceCategoryProvider(DeviceCategory.MOBILE)
            )

        val result = webPlatformInfoCollector.collect()

        result.deviceCategory shouldBe DeviceCategory.MOBILE
    }
}
