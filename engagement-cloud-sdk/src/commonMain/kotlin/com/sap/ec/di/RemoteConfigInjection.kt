package com.sap.ec.di

import com.sap.ec.remoteConfig.RemoteConfigHandler
import com.sap.ec.remoteConfig.RemoteConfigHandlerApi
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal object RemoteConfigInjection {
    val remoteConfigModules = module {
        single<RemoteConfigHandlerApi> {
            RemoteConfigHandler(
                deviceInfoCollector = get(),
                logConfigHolder = get(),
                sdkContext = get(),
                randomProvider = get(),
                embeddedMessagingContext = get(),
                sdkLogger = get { parametersOf(RemoteConfigHandler::class.simpleName) }
            )
        }
    }
}