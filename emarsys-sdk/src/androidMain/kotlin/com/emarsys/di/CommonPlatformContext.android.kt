package com.emarsys.di

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences

actual class CommonPlatformContext actual constructor() : PlatformContext {
    val application: Application by lazy {
        Class.forName("android.app.ActivityThread")
            .getMethod("currentApplication").invoke(null) as Application
    }
    val sharedPreferences: SharedPreferences by lazy {
        application.getSharedPreferences("emarsys-sdk", Context.MODE_PRIVATE)
    }

    val notificationManager: NotificationManager by lazy {
        application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}