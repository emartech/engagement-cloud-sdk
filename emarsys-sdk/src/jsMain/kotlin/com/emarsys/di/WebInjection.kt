package com.emarsys.di

import com.emarsys.JsEmarsysConfig
import com.emarsys.setup.config.JsEmarsysConfigStore
import com.emarsys.setup.config.SdkConfigStoreApi
import org.koin.core.module.Module
import org.koin.dsl.module

object WebInjection {
    val webModules = module {
        single<SdkConfigStoreApi<JsEmarsysConfig>> {
            JsEmarsysConfigStore(
                typedStorage = get()
            )
        }
    }
}

actual fun SdkKoinIsolationContext.loadPlatformModules(): List<Module> {
    return listOf(WebInjection.webModules)
}