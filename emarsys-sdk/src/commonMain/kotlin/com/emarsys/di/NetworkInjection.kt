package com.emarsys.di

import com.emarsys.core.channel.SdkEventDistributor
import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.networking.EmarsysClient
import com.emarsys.networking.clients.device.DeviceClient
import com.emarsys.networking.clients.device.DeviceClientApi
import com.emarsys.networking.clients.event.EventClient
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.SdkEvent
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClient
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClientApi
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object NetworkInjection {
    val networkModules = module {
        single<NetworkClientApi>(named(NetworkClientTypes.Generic)) {
            GenericNetworkClient(
                client = get<HttpClient>(),
                sdkLogger = get { parametersOf(GenericNetworkClient::class.simpleName) },
            )
        }
        single<NetworkClientApi>(named(NetworkClientTypes.Emarsys)) {
            EmarsysClient(
                networkClient = get<NetworkClientApi>(
                    named(NetworkClientTypes.Generic)
                ),
                sessionContext = get(),
                timestampProvider = get(),
                urlFactory = get(),
                json = get(),
                sdkLogger = get { parametersOf(EmarsysClient::class.simpleName) },
                sdkEventFlow = get<MutableSharedFlow<SdkEvent>>(named(EventFlowTypes.InternalEventFlow))
            )
        }
        single<EventClientApi> {
            EventClient(
                emarsysNetworkClient = get(named(NetworkClientTypes.Emarsys)),
                urlFactory = get(),
                json = get(),
                eventActionFactory = get(),
                sessionContext = get(),
                inAppConfigApi = get(),
                inAppPresenter = get(),
                inAppViewProvider = get(),
                sdkEventFlow = get<MutableSharedFlow<SdkEvent>>(named(EventFlowTypes.InternalEventFlow)),
                onlineSdkEventFlow = get<SdkEventDistributor>().onlineEvents,
                sdkLogger = get { parametersOf(EventClient::class.simpleName) },
                sdkDispatcher = get(named(DispatcherTypes.Sdk))
            )
        }
        single<DeviceClientApi> {
            DeviceClient(
                emarsysClient = get(named(NetworkClientTypes.Emarsys)),
                urlFactory = get(),
                deviceInfoCollector = get(),
                contactTokenHandler = get()
            )
        }
        single<RemoteConfigClientApi> {
            RemoteConfigClient(
                networkClient = get(named(NetworkClientTypes.Generic)),
                urlFactoryApi = get(),
                crypto = get(),
                json = get(),
                sdkLogger = get { parametersOf(RemoteConfigClient::class.simpleName) }
            )
        }
    }
}