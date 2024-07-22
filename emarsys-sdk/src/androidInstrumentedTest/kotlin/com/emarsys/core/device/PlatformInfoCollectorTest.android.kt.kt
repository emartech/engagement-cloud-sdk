package com.emarsys.core.device

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlatformInfoCollectorTest {
    private companion object {
        const val NOTIFICATION_MANAGER_IMPORTANCE = -1000
        const val CHANNEL_IMPORTANCE = 1
        const val CHANNEL_ID = "testChannelId"
        const val CHANNEL_NAME = "testChannelName"
    }

    private lateinit var platformInfoCollector: PlatformInfoCollector

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.apply {
        this.applicationInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE
    }

    @Before
    fun setup() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, CHANNEL_IMPORTANCE)
        notificationManager.createNotificationChannel(notificationChannel)
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
    fun testNotificationSettings_should_return_parsed_channel_settings() {
        val result = platformInfoCollector.notificationSettings()

        result.importance shouldBe NOTIFICATION_MANAGER_IMPORTANCE
        result.areNotificationsEnabled shouldBe true
        result.channelSettings.size shouldBe 1

        val settings = result.channelSettings.first()

        settings.channelId shouldBe CHANNEL_ID
        settings.importance shouldBe CHANNEL_IMPORTANCE
        settings.canBypassDnd shouldBe false
        settings.canShowBadge shouldBe true
        settings.shouldShowLights shouldBe false
        settings.shouldVibrate shouldBe false
    }
}