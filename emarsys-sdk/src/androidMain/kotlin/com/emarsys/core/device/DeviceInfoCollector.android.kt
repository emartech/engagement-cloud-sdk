package com.emarsys.core.device

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.ENGLISH

actual class DeviceInfoCollector(
    private val context: Context,
    private val languageProvider: LanguageProvider,
    private val isGooglePlayServicesAvailable: Boolean,
    private val isAutomaticPushSendingEnabled: Boolean
) {
    fun collect(): DeviceInfo {
        val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics

        return DeviceInfo(
            platform = if (isGooglePlayServicesAvailable) "android" else "android-huawei",
            applicationVersion = parseAppVersion() ?: UNKNOWN_VERSION_NAME,
            deviceModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            displayMetrics = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}",
            osVersion = Build.VERSION.RELEASE,
            sdkVersion = BuildConfig.VERSION_NAME,
            language = languageProvider.provideLanguage(),
            timezone = SimpleDateFormat("Z", ENGLISH).format(Calendar.getInstance().time),
            iOSPushSettings = null,
            androidNotificationSettings = null,
            isDebugMode = 0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE,
            hardwareId = "test-hardwareId",
        )
    }

    private fun parseAppVersion(): String? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace();
            return null
        }
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
}