package com.sap.ec.core.device

import android.os.Build

internal object SdkBuildConfig {
    fun getOsVersion(): String {
        return Build.VERSION.RELEASE
    }
}