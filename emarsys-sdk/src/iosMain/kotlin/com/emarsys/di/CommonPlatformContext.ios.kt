package com.emarsys.di

import com.emarsys.core.storage.StorageConstants
import platform.Foundation.NSUserDefaults

class IosPlatformContext: PlatformContext {

    val userDefaults: NSUserDefaults by lazy {
        NSUserDefaults(suiteName = StorageConstants.SUITE_NAME)
    }
}