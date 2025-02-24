package com.emarsys.core.launchapplication

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.emarsys.AndroidEmarsysConfig
import com.emarsys.FakeActivity
import com.emarsys.context.SdkContextApi
import com.emarsys.watchdog.activity.ActivityFinderApi
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test

class LaunchApplicationHandlerTest {

    private lateinit var launchApplicationHandler: LaunchApplicationHandler

    private lateinit var mockApplicationContext: Context
    private lateinit var mockActivityFinder: ActivityFinderApi
    private lateinit var mockSdkContext: SdkContextApi

    @Before
    fun setUp() {
        mockActivityFinder = mockk(relaxed = true)
        mockApplicationContext = mockk(relaxed = true)
        mockSdkContext = mockk(relaxed = true)
        launchApplicationHandler = LaunchApplicationHandler(
            mockApplicationContext,
            mockActivityFinder,
            mockSdkContext
        )

        coEvery { mockActivityFinder.currentActivity() } returns null
    }

    @Test
    fun testLaunchApplication_when_activityIsNull_inConfig() = runTest {
        val config = AndroidEmarsysConfig("testAppCode")
        every { mockSdkContext.config } returns config

        val mockIntent: Intent = mockk(relaxed = true)
        val mockPackageManager: PackageManager = mockk(relaxed = true)
        every { mockApplicationContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getLaunchIntentForPackage(any()) } returns mockIntent

        launchApplicationHandler.launchApplication()

        verify { mockPackageManager.getLaunchIntentForPackage(any()) }
        verify { mockApplicationContext.startActivity(mockIntent) }
    }

    @Test
    fun testLaunchApplication_when_activityIsSet_inConfig() = runTest {
        val config = AndroidEmarsysConfig("testAppCode", launchActivityClass = FakeActivity::class.java)
        every { mockSdkContext.config } returns config

        val intentCaptor = slot<Intent>()
        every { mockApplicationContext.startActivity(capture(intentCaptor)) } returns Unit

        launchApplicationHandler.launchApplication()

        val intent = intentCaptor.captured
        val expectedIntent = Intent(mockApplicationContext, FakeActivity::class.java)

        intent.component!!.className shouldBe expectedIntent.component!!.className
        verify { mockApplicationContext.startActivity(any()) }
    }

}
