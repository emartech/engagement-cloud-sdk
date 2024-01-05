package com.emarsys.core.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

class AndroidPlatformInfoCollector(
    private val context: Context,
): PlatformInfoCollectorApi {

    override fun collect(): String {
        val androidInfo = AndroidPlatformInfo(
            applicationVersion = parseAppVersion() ?: UNKNOWN_VERSION_NAME,
            osVersion = Build.VERSION.RELEASE,
            notificationSettings = null,
            isDebugMode = 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE,
        )
        
        return Json.encodeToString(androidInfo)
    }

    private fun parseAppVersion(): String? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return null
        }
    }

    private fun parseChannelSettings(): Pair<String, Any> {
        return "channelSettings" to listOf(JSONObject())
    }
}