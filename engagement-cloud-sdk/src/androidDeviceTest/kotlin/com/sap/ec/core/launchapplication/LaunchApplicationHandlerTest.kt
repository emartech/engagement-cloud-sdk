package com.sap.ec.core.launchapplication

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.sap.ec.FakeActivity
import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.device.AndroidVersionUtils.isBelowUpsideDownCake
import com.sap.ec.core.device.AndroidVersionUtils.isUpsideDownCake
import com.sap.ec.watchdog.activity.ActivityFinderApi
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import kotlin.test.Test

class LaunchApplicationHandlerTest {

    private lateinit var launchApplicationHandler: LaunchApplicationHandler

    private lateinit var mockApplicationContext: Context
    private lateinit var mockActivityFinder: ActivityFinderApi
    private lateinit var mockSdkContext: SdkContextApi

    private val intentCaptor = slot<Intent>()
    private val bundleCaptor = slot<Bundle>()

    companion object {
        const val BACKGROUND_ACTIVITY_CREATOR_START_MODE_KEY =
            "android.activity.pendingIntentCreatorBackgroundActivityStartMode"
        const val BACKGROUND_ACTIVITY_START_MODE_KEY = "android.pendingIntent.backgroundActivityAllowed"
    }

    @Before
    fun setUp() {
        mockkStatic(PendingIntent::class)
        mockActivityFinder = mockk(relaxed = true)
        mockApplicationContext = mockk(relaxed = true)
        mockSdkContext = mockk(relaxed = true)
        launchApplicationHandler = LaunchApplicationHandler(
            mockApplicationContext,
            mockActivityFinder,
            mockSdkContext,
            sdkLogger = mockk(relaxed = true)
        )

        coEvery { mockActivityFinder.currentActivity() } returns null
    }

    @After
    fun tearDown() {
        unmockkAll()
        intentCaptor.clear()
        bundleCaptor.clear()
    }

    @Test
    fun testLaunchApplication_when_activityIsNull_inConfig() = runTest {
        val config = AndroidEngagementCloudSDKConfig("testAppCode")
        every { mockSdkContext.config } returns config

        val expectedIntent = mockk<Intent>(relaxed = true)
        val mockPackageManager: PackageManager = mockk(relaxed = true)
        every { mockApplicationContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getLaunchIntentForPackage(any()) } returns expectedIntent

        val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
        every {
            PendingIntent.getActivity(
                mockApplicationContext,
                0,
                expectedIntent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                any()
            )
        } returns mockPendingIntent

        launchApplicationHandler.launchApplication()

        verify { mockPackageManager.getLaunchIntentForPackage(any()) }
        if (isBelowUpsideDownCake) {
            verify {
                PendingIntent.getActivity(
                    mockApplicationContext,
                    0,
                    expectedIntent,
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                    null
                )
            }
        } else {
            verify {
                PendingIntent.getActivity(
                    mockApplicationContext,
                    0,
                    expectedIntent,
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                    capture(bundleCaptor)
                )
            }
            if (isUpsideDownCake) {
                bundleCaptor.captured.getBoolean(BACKGROUND_ACTIVITY_START_MODE_KEY) shouldBe true
            } else {
                bundleCaptor.captured.getInt(BACKGROUND_ACTIVITY_CREATOR_START_MODE_KEY) shouldBe 1
            }
        }
        verify { mockPendingIntent.send() }
    }

    @Test
    fun testLaunchApplication_when_activityIsNull_inConfig_andLaunchIntentIsNull() =
        runTest {
            val config = AndroidEngagementCloudSDKConfig("testAppCode")
            every { mockSdkContext.config } returns config

            val mockPackageManager: PackageManager = mockk(relaxed = true)
            every { mockApplicationContext.packageManager } returns mockPackageManager
            every { mockPackageManager.getLaunchIntentForPackage(any()) } returns null

            launchApplicationHandler.launchApplication()

            verify { mockPackageManager.getLaunchIntentForPackage(any()) }
            verify(exactly = 0) {
                PendingIntent.getActivity(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            }
        }

    @Test
    fun testLaunchApplication_when_activityIsGiven_inConfig_andAndroidVersionIsBelow34() = runTest {
        val config =
            AndroidEngagementCloudSDKConfig("testAppCode", launchActivityClass = FakeActivity::class.java)
        every { mockSdkContext.config } returns config

        val mockPackageManager: PackageManager = mockk(relaxed = true)
        every { mockApplicationContext.packageManager } returns mockPackageManager
        val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
        every {
            PendingIntent.getActivity(
                mockApplicationContext,
                0,
                any(),
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                any()
            )
        } returns mockPendingIntent

        launchApplicationHandler.launchApplication()

        verify(exactly = 0) { mockApplicationContext.packageManager }
        if (isBelowUpsideDownCake) {
            verify {
                PendingIntent.getActivity(
                    mockApplicationContext,
                    0,
                    capture(intentCaptor),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                    null
                )
            }
        } else {
            verify {
                PendingIntent.getActivity(
                    mockApplicationContext,
                    0,
                    capture(intentCaptor),
                    FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE,
                    capture(bundleCaptor)
                )
            }
            if (isUpsideDownCake) {
                bundleCaptor.captured.getBoolean(BACKGROUND_ACTIVITY_START_MODE_KEY) shouldBe true
            } else {
                bundleCaptor.captured.getInt(BACKGROUND_ACTIVITY_CREATOR_START_MODE_KEY) shouldBe 1
            }

        }
        verify { mockPendingIntent.send() }
        intentCaptor.captured.component?.className shouldBe FakeActivity::class.qualifiedName
    }

}
