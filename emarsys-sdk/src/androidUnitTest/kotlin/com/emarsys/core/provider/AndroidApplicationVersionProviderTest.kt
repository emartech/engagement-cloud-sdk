package com.emarsys.core.provider

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.emarsys.core.device.UNKNOWN_VERSION_NAME
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.BeforeTest


class AndroidApplicationVersionProviderTest {
    private companion object {
        const val APP_VERSION = "2.0.0"
        const val PACKAGE_NAME = "packageName"
    }

    private lateinit var mockContext: Context
    private lateinit var appVersionProvider: AndroidApplicationVersionProvider

    @BeforeTest
    fun setup() {
        val mockPackageManager: PackageManager = mockk(relaxed = true)
        val packageInfo = PackageInfo()
        packageInfo.versionName = APP_VERSION

        mockContext = mockk(relaxed = true)
        every { mockContext.packageName } returns PACKAGE_NAME
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackageInfo(PACKAGE_NAME, 0) } returns packageInfo

        val applicationInfo = ApplicationInfo().apply { flags = 0 }
        every { mockContext.applicationInfo } returns applicationInfo

        appVersionProvider = AndroidApplicationVersionProvider(mockContext)
    }

    @Test
    fun testApplicationVersion_should_return_applicationVersion_from_packageInfo_if_available() {
        appVersionProvider.provide() shouldBe  APP_VERSION
    }

    @Test
    fun testApplicationVersion_should_return_UNKNOWN_when_packageInfo_is_not_available() {
        every { mockContext.packageManager.getPackageInfo(any<String>(), any<Int>()) } throws PackageManager.NameNotFoundException()
        appVersionProvider.provide() shouldBe UNKNOWN_VERSION_NAME
    }
}