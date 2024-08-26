package com.emarsys.di

import com.emarsys.core.storage.StorageConstants
import platform.Foundation.NSUserDefaults

actual class CommonPlatformContext actual constructor() : PlatformContext {

    val userDefaults: NSUserDefaults by lazy {
        NSUserDefaults(suiteName = StorageConstants.SUITE_NAME)
    }
}