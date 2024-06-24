package com.emarsys.core.provider

import com.emarsys.core.device.UNKNOWN_VERSION_NAME
import com.emarsys.core.providers.Provider
import platform.Foundation.NSBundle

class IosApplicationVersionProvider: Provider<String> {
    override fun provide(): String {
        return NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: UNKNOWN_VERSION_NAME
    }
}