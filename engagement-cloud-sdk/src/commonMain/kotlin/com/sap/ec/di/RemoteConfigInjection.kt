package com.sap.ec.di

import com.sap.ec.remoteConfig.RemoteConfigResponseHandler
import com.sap.ec.remoteConfig.RemoteConfigResponseHandlerApi
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

object RemoteConfigInjection {
    val remoteConfigModules = module {
        single<RemoteConfigResponseHandlerApi> {
            RemoteConfigResponseHandler(
                deviceInfoCollector = get(),
                sdkContext = get(),
                randomProvider = get(),
                sdkLogger = get { parametersOf(RemoteConfigResponseHandler::class.simpleName) }
            )
        }
    }
}