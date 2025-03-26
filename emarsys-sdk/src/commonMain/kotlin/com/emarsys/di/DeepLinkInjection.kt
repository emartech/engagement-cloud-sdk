package com.emarsys.di

import com.emarsys.api.deepLink.DeepLinkApi
import com.emarsys.api.deepLink.DeepLinkInternal
import com.emarsys.networking.clients.deepLink.DeepLinkClient
import com.emarsys.networking.clients.deepLink.DeepLinkClientApi
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object DeepLinkInjection {
    val deepLinkModules = module {
        single<DeepLinkClientApi> {
            DeepLinkClient(
                networkClient = get(named(NetworkClientTypes.Generic)),
                urlFactory = get(),
                userAgentProvider = get(),
                json = get(),
                sdkLogger = get { parametersOf(DeepLinkClient::class.simpleName) }
            )
        }
        singleOf(::DeepLinkInternal) { bind<DeepLinkApi>()}
    }
}