package com.emarsys.core.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test


class AndroidPlatformInfoCollectorTest {
    private companion object {
        private const val APP_VERSION = "2.0"
        private const val OS_VERSION = "testOSVersion"
    }

    private lateinit var mockContext: Context
    private lateinit var androidPlatformInfoCollector: AndroidPlatformInfoCollector
    private val json = Json

    @Before
    fun setup() {
        val mockPackageManager: PackageManager = mockk(relaxed = true)
        val packageName = "packageName"
        val packageInfo = PackageInfo()
        packageInfo.versionName = APP_VERSION

        mockContext = mockk(relaxed = true)
        every { mockContext.packageName } returns packageName
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo(packageName, 0) } returns packageInfo

        val applicationInfo = ApplicationInfo().apply { flags = 0 }
        every { mockContext.applicationInfo } returns applicationInfo

        mockkObject(SdkBuildConfig)
        every { SdkBuildConfig.getOsVersion() } returns OS_VERSION

        androidPlatformInfoCollector = AndroidPlatformInfoCollector(mockContext)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testCollect_should_collect_platformInfo() {
        val applicationInfo = ApplicationInfo().apply { flags = ApplicationInfo.FLAG_DEBUGGABLE }
        every { mockContext.applicationInfo } returns applicationInfo
        val expected = AndroidPlatformInfo(
            APP_VERSION,
            OS_VERSION,
            null,
            true
        )

        val result = androidPlatformInfoCollector.collect()

        val platformInfo = json.decodeFromString<AndroidPlatformInfo>(result)

        platformInfo shouldBe expected
    }

    @Test
    fun testCollect_should_collect_platformInfo_debugMode_false() {
        val expected = AndroidPlatformInfo(
            APP_VERSION,
            OS_VERSION,
            null,
            false
        )

        val result = androidPlatformInfoCollector.collect()

        val platformInfo = json.decodeFromString<AndroidPlatformInfo>(result)

        platformInfo shouldBe expected
    }

    @Test
    fun testApplicationVersion_should_return_applicationVersion_from_packageInfo_if_available() {
        androidPlatformInfoCollector.applicationVersion() shouldBe APP_VERSION
    }
    @Test
    fun testApplicationVersion_should_return_UNKNOWN_when_packageInfo_is_not_available() {
        every { mockContext.packageManager.getPackageInfo(any<String>(), any<Int>()) } throws PackageManager.NameNotFoundException()
        androidPlatformInfoCollector.applicationVersion() shouldBe UNKNOWN_VERSION_NAME
    }
}