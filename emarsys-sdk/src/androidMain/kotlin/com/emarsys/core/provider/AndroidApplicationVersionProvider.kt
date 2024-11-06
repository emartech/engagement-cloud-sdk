package com.emarsys.core.provider

import android.content.Context
import com.emarsys.core.device.UNKNOWN_VERSION_NAME
import com.emarsys.core.providers.Provider

class AndroidApplicationVersionProvider(private val context: Context): Provider<String> {
    override fun provide(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: UNKNOWN_VERSION_NAME
        } catch (e: Exception) {
            e.printStackTrace()
            return UNKNOWN_VERSION_NAME
        }
    }
}