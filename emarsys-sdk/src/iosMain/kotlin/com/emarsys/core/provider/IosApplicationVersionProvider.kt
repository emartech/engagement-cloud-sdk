package com.emarsys.core.provider

import com.emarsys.core.device.UNKNOWN_VERSION_NAME
import com.emarsys.core.providers.ApplicationVersionProviderApi
import platform.Foundation.NSBundle

internal class IosApplicationVersionProvider: ApplicationVersionProviderApi {
    override fun provide(): String {
        return NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: UNKNOWN_VERSION_NAME
    }
}