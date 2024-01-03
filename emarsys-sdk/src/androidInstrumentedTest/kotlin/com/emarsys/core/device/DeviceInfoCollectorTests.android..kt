package com.emarsys.core.device

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

class DeviceInfoCollectorTests {

    private companion object {
        private const val LANGUAGE = "en-US"
        private const val APP_VERSION = "2.0"
    }

    private lateinit var testAndroidDeviceInfoCollector: AndroidDeviceInfoCollector
    private lateinit var mockLanguageProvider: LanguageProvider
    private lateinit var timeZone: TimeZone
    private lateinit var context: Context
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        context = getInstrumentation().targetContext.applicationContext

        val packageManager: PackageManager = mockk(relaxed = true)
        val packageName = "packageName"
        val packageInfo = PackageInfo()
        packageInfo.versionName = APP_VERSION

        mockContext = getApplication { flags = ApplicationInfo.FLAG_DEBUGGABLE }
        every { mockContext.contentResolver } returns context.contentResolver
        every { mockContext.packageName } returns packageName
        every { mockContext.packageManager } returns packageManager
        every { packageManager.getPackageInfo(packageName, 0) } returns packageInfo

        timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        TimeZone.setDefault(timeZone)

        mockLanguageProvider = mockk(relaxed = true)
        every { mockLanguageProvider.provideLanguage() } returns LANGUAGE
    }

    @After
    fun teardown() {
        TimeZone.setDefault(null)
    }

    @Test
    fun test_collect_shouldReturn_deviceInfo() {
        val expectedPlatformInfo = AndroidDeviceInfo(
            applicationVersion = APP_VERSION,
            osVersion = Build.VERSION.RELEASE,
            null,
            isDebugMode = true,
        )
        val expectedDeviceInfo = DeviceInformation(
            platform = "android",
            manufacturer = Build.MANUFACTURER,
            displayMetrics = "${Resources.getSystem().displayMetrics.widthPixels}x${Resources.getSystem().displayMetrics.heightPixels}",
            model = Build.MODEL,
            sdkVersion = BuildConfig.VERSION_NAME,
            language = LANGUAGE,
            timezone = "+0900",
            hardwareId = "test hwid",
            platformInfo = Json.encodeToString(expectedPlatformInfo)
        )
        testAndroidDeviceInfoCollector = AndroidDeviceInfoCollector(mockContext, true)
        val deviceInfoCollector =
            DeviceInfoCollector(testAndroidDeviceInfoCollector, mockLanguageProvider)

        val result = deviceInfoCollector.collect()

        result shouldBe Json.encodeToString(expectedDeviceInfo)
        verify { mockLanguageProvider.provideLanguage() }
    }

    @Test
    fun test_collect_platformShouldBe_huawei() {
        testAndroidDeviceInfoCollector = AndroidDeviceInfoCollector(mockContext, false)
        val deviceInfoCollector =
            DeviceInfoCollector(testAndroidDeviceInfoCollector, mockLanguageProvider)

        val result = deviceInfoCollector.collect()

        val deviceInfo = Json.decodeFromString<DeviceInformation>(result)

        deviceInfo.platform shouldBe "android-huawei"
    }

    @Test
    @Throws(PackageManager.NameNotFoundException::class)
    fun test_collect_applicationVersion_shouldBeDefault_whenVersionInPackageInfo_isNull() {
        val packageName = "packageName"
        val mockContext: Context = mockk()
        val packageInfo = PackageInfo()
        val packageManager: PackageManager = mockk()
        packageInfo.versionName = null
        every { mockContext.contentResolver } returns context.contentResolver
        every { mockContext.packageName } returns (packageName)
        every { mockContext.packageManager } returns (packageManager)
        every { packageManager.getPackageInfo(packageName, 0) } returns (packageInfo)
        every { mockContext.applicationInfo } returns (mockk())

        testAndroidDeviceInfoCollector = AndroidDeviceInfoCollector(mockContext, false)
        val deviceInfoCollector =
            DeviceInfoCollector(testAndroidDeviceInfoCollector, mockLanguageProvider)

        val result = deviceInfoCollector.collect()

        val deviceInfo = Json.decodeFromString<DeviceInformation>(result)

        val platformInfo = Json.decodeFromString<AndroidDeviceInfo>(deviceInfo.platformInfo)

        platformInfo.applicationVersion shouldBe UNKNOWN_VERSION_NAME
    }

    @Test
    fun test_collect_isDebugMode_shouldBeFalse_withReleaseApplication() {
        val mockReleaseContext = getApplication { flags = 0 }
        testAndroidDeviceInfoCollector = AndroidDeviceInfoCollector(mockReleaseContext, false)
        val debugDeviceInfo =
            DeviceInfoCollector(testAndroidDeviceInfoCollector, mockLanguageProvider)

        val result = debugDeviceInfo.collect()

        val deviceInfo = Json.decodeFromString<DeviceInformation>(result)

        val platformInfo = Json.decodeFromString<AndroidDeviceInfo>(deviceInfo.platformInfo)

        platformInfo.isDebugMode.shouldBeFalse()
    }

    private fun getApplication(init: ApplicationInfo.() -> Unit) =
        (spyk<Context>(context.applicationContext) as Application).also {
            every { it.applicationInfo } returns (ApplicationInfo().apply(init))
        }
}