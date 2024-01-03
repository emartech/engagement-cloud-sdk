package com.emarsys.core.device

import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale.ENGLISH

actual class DeviceInfoCollector(
    private val androidDeviceInfoCollector: AndroidDeviceInfoCollector,
    private val languageProvider: LanguageProvider,
): DeviceInfoCollectorApi {
    actual override fun collect(): String {
        val androidDeviceInfo = androidDeviceInfoCollector.collect()
        val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics

        val deviceInfo = DeviceInformation(
            platform = androidDeviceInfoCollector.getPlatform(),
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            sdkVersion = BuildConfig.VERSION_NAME,
            displayMetrics = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}",
            language = languageProvider.provideLanguage(),
            timezone = SimpleDateFormat("Z", ENGLISH).format(Calendar.getInstance().time),
            hardwareId = "test hwid",
            platformInfo = androidDeviceInfo
        )

        return Json.encodeToString(deviceInfo)
    }
}