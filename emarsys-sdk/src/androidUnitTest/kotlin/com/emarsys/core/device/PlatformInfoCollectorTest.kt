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
import org.junit.After
import org.junit.Before
import org.junit.Test


class PlatformInfoCollectorTest {
    private companion object {
        private const val APP_VERSION = "2.0"
        private const val OS_VERSION = "testOSVersion"
    }

    private lateinit var mockContext: Context
    private lateinit var platformInfoCollector: PlatformInfoCollector

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

        platformInfoCollector = PlatformInfoCollector(mockContext)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testIsDebugMode_should_return_true() {
        val applicationInfo = ApplicationInfo().apply { flags = ApplicationInfo.FLAG_DEBUGGABLE }
        every { mockContext.applicationInfo } returns applicationInfo

        val result = platformInfoCollector.isDebugMode()

        result shouldBe true
    }

    @Test
    fun testIsDebugMode_should_return_false() {
        val applicationInfo = ApplicationInfo()
        every { mockContext.applicationInfo } returns applicationInfo

        val result = platformInfoCollector.isDebugMode()

        result shouldBe false
    }
}