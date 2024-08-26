package com.emarsys.di

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import com.emarsys.applicationContext
import com.emarsys.core.storage.StorageConstants

actual class CommonPlatformContext actual constructor() : PlatformContext {

    val sharedPreferences: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(StorageConstants.SUITE_NAME, Context.MODE_PRIVATE)
    }

    val notificationManager: NotificationManager by lazy {
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}