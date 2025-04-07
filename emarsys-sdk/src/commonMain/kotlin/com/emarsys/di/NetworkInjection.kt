package com.emarsys.di

import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.networking.EmarsysClient
import com.emarsys.networking.clients.config.ConfigClient
import com.emarsys.networking.clients.device.DeviceClient
import com.emarsys.networking.clients.device.DeviceClientApi
import com.emarsys.networking.clients.event.EventClient
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.logging.LoggingClient
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClient
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClientApi
import io.ktor.client.HttpClient
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
                sdkEventDistributor = get()
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
                sdkEventManager = get(),
                eventsDao = get(),
                sdkLogger = get { parametersOf(EventClient::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application))
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
        single<ConfigClient> {
            ConfigClient(
                emarsysNetworkClient = get(named(NetworkClientTypes.Emarsys)),
                urlFactory = get(),
                sdkEventDistributor = get(),
                sessionContext = get(),
                sdkContext = get(),
                contactTokenHandler = get(),
                json = get(),
                sdkLogger = get { parametersOf(ConfigClient::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application))
            )
        }
        single<LoggingClient> {
            LoggingClient(
                emarsysNetworkClient = get(named(NetworkClientTypes.Emarsys)),
                urlFactory = get(),
                sdkEventManager = get(),
                json = get(),
                sdkLogger = get { parametersOf(LoggingClient::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                deviceInfoCollector = get(),
                eventsDao = get()
            )
        }
    }
}