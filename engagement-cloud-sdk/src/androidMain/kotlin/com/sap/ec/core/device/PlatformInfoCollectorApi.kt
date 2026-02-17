package com.sap.ec.core.device

import android.util.DisplayMetrics

interface PlatformInfoCollectorApi {
    fun displayMetrics(): DisplayMetrics?
    fun isDebugMode(): Boolean
}