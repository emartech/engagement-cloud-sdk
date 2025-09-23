package com.emarsys.core.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.util.DisplayMetrics

class PlatformInfoCollector(
    private val context: Context,
) : PlatformInfoCollectorApi {

    override fun displayMetrics(): DisplayMetrics? {
        return Resources.getSystem().displayMetrics
    }

    override fun isDebugMode(): Boolean {
        return 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    }
}