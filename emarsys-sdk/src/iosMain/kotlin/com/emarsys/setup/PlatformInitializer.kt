package com.emarsys.setup

import com.emarsys.di.IosInjection
import com.emarsys.di.SdkKoinIsolationContext.koin

internal class PlatformInitializer : PlatformInitializerApi {

    override suspend fun init() {
        koin.loadModules(listOf(IosInjection.iosModules))
    }

}