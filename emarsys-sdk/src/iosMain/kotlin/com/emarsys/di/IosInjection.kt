package com.emarsys.di

import com.emarsys.EmarsysConfig
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.StringStorage
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.setup.PlatformInitializer
import com.emarsys.setup.PlatformInitializerApi
import com.emarsys.setup.config.IosSdkConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

object IosInjection {
    val iosModules = module {
        single<NSUserDefaults> { NSUserDefaults(StorageConstants.SUITE_NAME) }
        single<StringStorageApi> { StringStorage(userDefaults = get()) }
        single<SdkConfigStoreApi<EmarsysConfig>> {
            IosSdkConfigStore(
                typedStorage = get()
            )
        }
        single<PlatformInitializerApi> { PlatformInitializer() }
    }
}

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(IosInjection.iosModules)
}