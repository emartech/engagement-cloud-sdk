package com.emarsys.core.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

class AndroidPlatformInfoCollector(
    private val context: Context,
): PlatformInfoCollectorApi {

    override fun collect(): String {
        val androidInfo = AndroidPlatformInfo(
            applicationVersion = applicationVersion(),
            osVersion = SdkBuildConfig.getOsVersion(),
            notificationSettings = null,
            isDebugMode = 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE,
        )
        
        return Json.encodeToString(androidInfo)
    }

    override fun applicationVersion(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            e.printStackTrace()
            return UNKNOWN_VERSION_NAME
        }
    }

    private fun parseChannelSettings(): Pair<String, Any> {
        return "channelSettings" to listOf(JSONObject())
    }
}