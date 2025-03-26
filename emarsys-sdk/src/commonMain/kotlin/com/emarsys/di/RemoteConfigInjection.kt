package com.emarsys.di

import com.emarsys.remoteConfig.RemoteConfigHandler
import com.emarsys.remoteConfig.RemoteConfigHandlerApi
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

object RemoteConfigInjection {
    val remoteConfigModules = module {
        single<RemoteConfigHandlerApi> {
            RemoteConfigHandler(
                remoteConfigClient = get(),
                deviceInfoCollector = get(),
                sdkContext = get(),
                randomProvider = get(),
                sdkLogger = get { parametersOf(RemoteConfigHandler::class.simpleName) }
            )
        }
    }
}