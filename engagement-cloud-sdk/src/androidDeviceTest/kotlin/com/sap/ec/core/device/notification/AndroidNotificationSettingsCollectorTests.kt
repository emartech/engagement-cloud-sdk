package com.sap.ec.core.device.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test

class AndroidNotificationSettingsCollectorTests {
    private companion object {
        const val CHANNEL_IMPORTANCE = 1
        const val DEBUG_CHANNEL_ID = "ems_debug"
        const val CHANNEL_ID = "testChannelId"
        const val CHANNEL_NAME = "testChannelName"
    }


    private val context = InstrumentationRegistry.getInstrumentation().targetContext.apply {
        this.applicationInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var androidNotificationSettingsCollector: AndroidNotificationSettingsCollector

    @Before
    fun setup() {
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.deleteNotificationChannel(CHANNEL_ID)
        notificationManager.deleteNotificationChannel(DEBUG_CHANNEL_ID)

        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, CHANNEL_IMPORTANCE)
        notificationManager.createNotificationChannel(notificationChannel)

        androidNotificationSettingsCollector = AndroidNotificationSettingsCollector(context)
    }

    @Test
    fun testNotificationSettings_should_return_parsed_channel_settings() {
        val result = androidNotificationSettingsCollector.collect()

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