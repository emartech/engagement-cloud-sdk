package com.emarsys.core.device

import android.util.DisplayMetrics

interface PlatformInfoCollectorApi {
    fun notificationSettings(): AndroidNotificationSettings
    fun displayMetrics(): DisplayMetrics?
    fun isDebugMode(): Boolean
}