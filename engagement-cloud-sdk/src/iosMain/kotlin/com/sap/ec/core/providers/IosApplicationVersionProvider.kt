package com.sap.ec.core.providers

import com.sap.ec.core.device.UNKNOWN_VERSION_NAME
import platform.Foundation.NSBundle

internal class IosApplicationVersionProvider: ApplicationVersionProviderApi {
    override fun provide(): String {
        return NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: UNKNOWN_VERSION_NAME
    }
}