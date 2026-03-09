package com.sap.ec.core.device

import android.util.DisplayMetrics

internal interface PlatformInfoCollectorApi {
    fun displayMetrics(): DisplayMetrics?
    fun isDebugMode(): Boolean
}