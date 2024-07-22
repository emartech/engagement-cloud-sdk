package com.emarsys.core.device

import android.content.Context
import android.content.pm.ApplicationInfo
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class PlatformInfoCollectorTest {
    private lateinit var mockContext: Context
    private lateinit var platformInfoCollector: PlatformInfoCollector

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        platformInfoCollector = PlatformInfoCollector(mockContext)
    }

    @Test
    fun testIsDebugMode_should_return_false() {
        val applicationInfo = ApplicationInfo().apply { flags = 0 }
        every { mockContext.applicationInfo } returns applicationInfo

        val result = platformInfoCollector.isDebugMode()

        result shouldBe false
    }
}