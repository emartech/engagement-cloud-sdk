package com.emarsys.core.device

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

actual class DeviceInfoCollector(context:Context) {
    fun collect(): DeviceInfo {
        return DeviceInfo(
            "android",
            "1.0.0",
            Build.MODEL,
            Build.MANUFACTURER,
            "7.0",
            Build.VERSION.RELEASE,
            "en-us",
            SimpleDateFormat("Z", Locale.ENGLISH).format(Calendar.getInstance().time),
            null,
            null,
            "hardwareId"
        )
    }

    actual fun collectDeviceInfoRequest(): String {
        val deviceInfo = collect()
        val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics

        return JSONObject(
            mapOf(
                "notificationSettings" to mapOf(
                    parseChannelSettings(),
                    //"importance" to notificationSettings.importance,
                    //  "areNotificationsEnabled" to notificationSettings.areNotificationsEnabled
                ),
                "hwid" to deviceInfo.hardwareId,
                "platform" to deviceInfo.platform,
                "language" to deviceInfo.language,
                "timezone" to deviceInfo.timezone,
                "manufacturer" to deviceInfo.manufacturer,
                "model" to deviceInfo.deviceModel,
                "osVersion" to deviceInfo.osVersion,
                "displayMetrics" to "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}",
                "sdkVersion" to deviceInfo.sdkVersion,
                "appVersion" to deviceInfo.applicationVersion
            )
        ).toString()
    }

    private fun parseChannelSettings(): Pair<String, Any> {

        return "channelSettings" to listOf(JSONObject())
    }

    actual fun deviceType(): String {
        return Build.MODEL
    }

    actual fun osVersion(): String {
        return Build.VERSION.RELEASE
    }

    actual fun hardwareId(): String {
        return "testHardwareId"
    }
}