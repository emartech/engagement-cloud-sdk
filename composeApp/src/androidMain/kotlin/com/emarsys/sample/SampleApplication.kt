package com.emarsys.sample

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        createNotificationChannel(
            "ems_sample_news",
            "News",
            "News and updates go into this channel"
        )
        createNotificationChannel(
            "ems_sample_messages",
            "Messages",
            "Important messages go into this channel"
        )
    }

    private fun createNotificationChannel(
        id: String,
        name: String,
        description: String
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = description
        notificationManager.createNotificationChannel(notificationChannel)
    }
}