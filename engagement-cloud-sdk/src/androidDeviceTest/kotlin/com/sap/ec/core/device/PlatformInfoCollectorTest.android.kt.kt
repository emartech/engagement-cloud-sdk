package com.sap.ec.core.device

import android.content.pm.ApplicationInfo
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlatformInfoCollectorTest {
    private lateinit var platformInfoCollector: PlatformInfoCollector

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.apply {
        this.applicationInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE
    }

    @Before
    fun setup() {
        platformInfoCollector = PlatformInfoCollector(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testIsDebugMode_should_return_true() {
        val result = platformInfoCollector.isDebugMode()

        result shouldBe true
    }

    @Test
    fun testIsDebugMode_should_return_false() {
        InstrumentationRegistry.getInstrumentation().targetContext.apply {
            this.applicationInfo.flags = 0
        }

        val result = platformInfoCollector.isDebugMode()

        result shouldBe false
    }
}