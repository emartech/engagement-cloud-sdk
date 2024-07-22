package com.emarsys.core.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.Resources
import android.util.DisplayMetrics
import org.json.JSONObject

class PlatformInfoCollector(
    private val context: Context,
) : PlatformInfoCollectorApi {

    override fun notificationSettings(): AndroidNotificationSettings {
        TODO("Not yet implemented")
    }

    override fun displayMetrics(): DisplayMetrics? {
        return Resources.getSystem().displayMetrics
    }

    override fun isDebugMode(): Boolean {
        return 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
    }

    private fun parseChannelSettings(): Pair<String, Any> {
        return "channelSettings" to listOf(JSONObject())
    }
}