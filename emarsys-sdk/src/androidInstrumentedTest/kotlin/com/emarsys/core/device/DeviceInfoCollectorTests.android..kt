package com.emarsys.core.device

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.emarsys.core.util.AndroidVersionUtils
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.json.JSONObject
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.Test

class DeviceInfoCollectorTests {

    private companion object {
        private const val LANGUAGE = "en-US"
        private const val APP_VERSION = "2.0"
    }

    private lateinit var deviceInfoCollector: DeviceInfoCollector
    private lateinit var tz: TimeZone
    private lateinit var context: Context

    private lateinit var mockLanguageProvider: LanguageProvider

    private lateinit var mockNotificationManagerHelper: NotificationSettings

    @BeforeTest
    fun setup() {
        tz = TimeZone.getTimeZone("Asia/Tokyo")
        TimeZone.setDefault(tz)
        context = getInstrumentation().targetContext.applicationContext

        mockLanguageProvider = mockk()
        every { mockLanguageProvider.provideLanguage() } returns LANGUAGE

        deviceInfoCollector = DeviceInfoCollector(context, mockLanguageProvider, true, true)
    }

    @AfterTest
    fun teardown() {
        TimeZone.setDefault(null)
    }

    @Test
    fun testCollect_initializesFields() {
        val deviceInfo = deviceInfoCollector.collect()
        with(deviceInfo) {
            hardwareId shouldNotBe null
            platform shouldNotBe null
            language shouldNotBe null
            timezone shouldNotBe null
            manufacturer shouldNotBe null
            deviceModel shouldNotBe null
            applicationVersion shouldNotBe null
            osVersion shouldNotBe null
            displayMetrics shouldNotBe null
            sdkVersion shouldNotBe null

        }
    }

    @Test
    @Throws(PackageManager.NameNotFoundException::class)
    fun testGetApplicationVersion_shouldBeDefault_whenVersionInPackageInfo_isNull() {
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

        val deviceInfo = deviceInfoCollector.collect()

        deviceInfo.applicationVersion shouldBe UNKNOWN_VERSION_NAME
    }

    @Test
    fun testTimezoneCorrectlyFormatted() {
        val deviceInfo = deviceInfoCollector.collect()

        "+0900" shouldBe deviceInfo.timezone
    }

    @Test
    fun testTimezoneCorrectlyFormatted_withArabicLocale() {
        val previous = Locale.getDefault()
        val locale = Locale("my")
        val resources = context.resources
        Locale.setDefault(locale)
        val config = resources.configuration
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
        Locale.setDefault(previous)

        val deviceInfo = deviceInfoCollector.collect()

        "+0900" shouldBe deviceInfo.timezone
    }

    @Test
    fun testGetDisplayMetrics() {
        val deviceInfo = deviceInfoCollector.collect()
        deviceInfo.displayMetrics shouldBe Resources.getSystem().displayMetrics
    }

    @Test
    fun testIsDebugMode_withDebugApplication() {
        val mockDebugContext = getApplication { flags = ApplicationInfo.FLAG_DEBUGGABLE }
        val debugDeviceInfo = DeviceInfoCollector(
            mockDebugContext,
            mockLanguageProvider,
            isAutomaticPushSendingEnabled = true,
            isGooglePlayServicesAvailable = true
        )
        val deviceInfo = debugDeviceInfo.collect()
        deviceInfo.isDebugMode.shouldBeTrue()
    }

    @Test
    fun testIsDebugMode_withReleaseApplication() {
        val mockReleaseContext = getApplication { flags = 0 }
        val debugDeviceInfo = DeviceInfoCollector(
            mockReleaseContext,
            mockLanguageProvider,
            isAutomaticPushSendingEnabled = true,
            isGooglePlayServicesAvailable = true
        )
        val deviceInfo = deviceInfoCollector.collect()

        deviceInfo.isDebugMode.shouldBeFalse()
    }

    @Test
    fun testGetLanguage_isAcquiredFromLanguageProvider() {
        val deviceInfo = deviceInfoCollector.collect()

        val language = deviceInfo.language
        verify { mockLanguageProvider.provideLanguage() }
        LANGUAGE shouldBe language
    }

    @Test
    fun testGetDeviceInfoPayload_shouldEqualPayload() {
        val packageName = "packageName"
        val mockContext: Context = mockk()
        val packageInfo = PackageInfo()
        val packageManager: PackageManager = mockk()
        packageInfo.versionName = APP_VERSION
        every { mockContext.contentResolver } returns context.contentResolver
        every { mockContext.packageName } returns packageName
        every { mockContext.packageManager } returns packageManager
        every { packageManager.getPackageInfo(packageName, 0) } returns (packageInfo)
        every { mockContext.applicationInfo } returns mockk()

        every { mockNotificationManagerHelper.channelSettings } returns
                listOf(
                    ChannelSettings(
                        channelId = "channelId"
                    )
                )


        var channelSettings = """
        channelSettings: [
            {
                "channelId":"channelId",
                "importance":-1000,
                "isCanBypassDnd":false,
                "isCanShowBadge":false,
                "isShouldVibrate":false
            }
        ]"""

        if (!AndroidVersionUtils.isOreoOrAbove) {
            channelSettings = "channelSettings: [{}]"
        }
        val expectedPayload = JSONObject(
            """{
                  "notificationSettings": {
                    $channelSettings,
                    "importance": 0,
                    "areNotificationsEnabled": false
                  },
                  "hwid": "hwid",
                  "platform": "android",
                  "language": "en-US",
                  "timezone": "+0900",
                  "manufacturer": "${Build.MANUFACTURER}",
                  "model": "${Build.MODEL}",
                  "osVersion": "${Build.VERSION.RELEASE}",
                  "displayMetrics": "${Resources.getSystem().displayMetrics.widthPixels}x${Resources.getSystem().displayMetrics.heightPixels}",
                  "sdkVersion": "sdkVersion",
                  "appVersion": "$APP_VERSION" 
                }"""
        ).toString()
        deviceInfoCollector.collectDeviceInfoRequest() shouldBe expectedPayload
    }

    @Test
    fun testDeviceInfo_platformShouldBeHuawei() {
        val packageName = "packageName"
        val mockContext: Context = mockk()
        val packageInfo = PackageInfo()
        val packageManager: PackageManager = mockk()
        packageInfo.versionName = APP_VERSION
        every { mockContext.contentResolver } returns (context.contentResolver)
        every { mockContext.packageName } returns (packageName)
        every { mockContext.packageManager } returns (packageManager)
        every { packageManager.getPackageInfo(packageName, 0) } returns (packageInfo)
        every { mockContext.applicationInfo } returns (mockk())

        val deviceInfo = deviceInfoCollector.collect()

        deviceInfo.platform shouldBe "android-huawei"
    }

    private fun getApplication(init: ApplicationInfo.() -> Unit) =
        (spyk<Context>(context.applicationContext) as Application).also {
            every { it.applicationInfo } returns (ApplicationInfo().apply(init))
        }
}