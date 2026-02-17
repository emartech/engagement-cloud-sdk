package com.sap.ec.di

import com.sap.ec.api.deeplink.DeepLinkApi
import com.sap.ec.api.deeplink.DeepLinkInternal
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.deepLink.DeepLinkClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object DeepLinkInjection {
    val deepLinkModules = module {
        single<EventBasedClientApi>(named(EventBasedClientTypes.DeepLink)) {
            DeepLinkClient(
                networkClient = get(named(NetworkClientTypes.Generic)),
                clientExceptionHandler = get(),
                sdkEventManager = get(),
                urlFactory = get(),
                userAgentProvider = get(),
                eventsDao = get(),
                json = get(),
                sdkLogger = get { parametersOf(DeepLinkClient::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application))
            )
        }
        singleOf(::DeepLinkInternal) { bind<DeepLinkApi>() }
    }
}