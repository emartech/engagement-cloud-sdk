package com.emarsys.core.device

import android.util.DisplayMetrics

interface PlatformInfoCollectorApi {
    fun displayMetrics(): DisplayMetrics?
    fun isDebugMode(): Boolean
}