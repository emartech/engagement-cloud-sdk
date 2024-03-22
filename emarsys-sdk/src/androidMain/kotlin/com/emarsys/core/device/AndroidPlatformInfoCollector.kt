package com.emarsys.core.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.util.DisplayMetrics
import org.json.JSONObject

class AndroidPlatformInfoCollector(
    private val context: Context,
): PlatformInfoCollectorApi {

    override fun collect(): AndroidPlatformInfo {
        return AndroidPlatformInfo(
            osVersion = SdkBuildConfig.getOsVersion(),
            notificationSettings = null,
            isDebugMode = 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE,
        )
    }

    private fun getDisplayMetrics(): DisplayMetrics? {
        return Resources.getSystem().displayMetrics
    }

    private fun parseChannelSettings(): Pair<String, Any> {
        return "channelSettings" to listOf(JSONObject())
    }
}