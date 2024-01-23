package com.emarsys.core.device

import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import com.emarsys.core.storage.StorageApi
import com.emarsys.providers.Provider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale.ENGLISH

actual class DeviceInfoCollector(
    private val androidPlatformInfoCollector: PlatformInfoCollectorApi,
    private val languageProvider: LanguageProvider,
    private val uuidProvider: Provider<String>,
    private val storage: StorageApi<String>,
    private val isGooglePlayServicesAvailable: Boolean,
): DeviceInfoCollectorApi {
    private companion object {
        const val HARDWARE_ID_STORAGE_KEY = "hardwareId"
    }

    actual override fun collect(): String {
        val androidDeviceInfo = androidPlatformInfoCollector.collect()
        val displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics

        val deviceInfo = DeviceInformation(
            platform = getPlatform(),
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            sdkVersion = BuildConfig.VERSION_NAME,
            displayMetrics = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}",
            language = languageProvider.provideLanguage(),
            timezone = SimpleDateFormat("Z", ENGLISH).format(Calendar.getInstance().time),
            hardwareId = getHardwareId(),
            platformInfo = androidDeviceInfo,
            applicationVersion = androidPlatformInfoCollector.applicationVersion()
        )

        return Json.encodeToString(deviceInfo)
    }

    override fun getHardwareId(): String {
        return storage.get(HARDWARE_ID_STORAGE_KEY) ?: run {
            val generatedId = uuidProvider.provide()
            storage.put(HARDWARE_ID_STORAGE_KEY, generatedId)
            generatedId
        }
    }

    private fun getPlatform(): String {
        return if (isGooglePlayServicesAvailable) "android" else "android-huawei"
    }
}