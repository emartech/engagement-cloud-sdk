package com.emarsys.core.resource

import android.content.Context
import android.content.pm.PackageManager

class MetadataReader(private val context: Context) {

    fun getInt(key: String): Int {
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            applicationInfo.metaData.getInt(key)
        } catch (ignored: PackageManager.NameNotFoundException) {
            0
        }
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return getInt(key).let {
            if (it != 0) it else defaultValue
        }
    }
}