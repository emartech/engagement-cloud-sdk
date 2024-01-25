package com.emarsys.di

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.emarsys.applicationContext

actual class CommonPlatformContext actual constructor() : PlatformContext {

    val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences("emarsys-sdk", Context.MODE_PRIVATE)
    }

    val notificationManager: NotificationManager by lazy {
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}