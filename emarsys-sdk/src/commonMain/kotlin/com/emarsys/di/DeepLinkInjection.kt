package com.emarsys.di

import com.emarsys.api.deepLink.DeepLinkApi
import com.emarsys.api.deepLink.DeepLinkInternal
import com.emarsys.networking.clients.deepLink.DeepLinkClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object DeepLinkInjection {
    val deepLinkModules = module {
        single<DeepLinkClient> {
            DeepLinkClient(
                networkClient = get(named(NetworkClientTypes.Generic)),
                sdkEventFlow = get(named(EventFlowTypes.InternalEventFlow)),
                urlFactory = get(),
                userAgentProvider = get(),
                json = get(),
                sdkLogger = get { parametersOf(DeepLinkClient::class.simpleName) },
                sdkDispatcher = get(named(DispatcherTypes.Sdk))
            )
        }
        singleOf(::DeepLinkInternal) { bind<DeepLinkApi>() }
    }
}