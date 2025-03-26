package com.emarsys.di

import com.emarsys.EmarsysConfig
import com.emarsys.core.storage.StorageConstants
import com.emarsys.setup.config.IosSdkConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

object IosInjection {
    val iosModules = module {
        single<NSUserDefaults> { NSUserDefaults(StorageConstants.SUITE_NAME) }
        single<SdkConfigStoreApi<EmarsysConfig>> {
            IosSdkConfigStore(
                typedStorage = get()
            )
        }
    }
}

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(IosInjection.iosModules)
}