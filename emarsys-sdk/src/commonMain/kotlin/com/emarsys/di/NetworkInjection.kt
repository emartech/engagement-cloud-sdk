package com.emarsys.di

import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.networking.EmarsysClient
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.config.ConfigClient
import com.emarsys.networking.clients.device.DeviceClient
import com.emarsys.networking.clients.event.EventClient
import com.emarsys.networking.clients.logging.LoggingClient
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClient
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
        single<EventBasedClientApi>(named(EventBasedClientTypes.Event)) {
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
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                uuidProvider = get()
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.Device)) {
            DeviceClient(
                emarsysClient = get(named(NetworkClientTypes.Emarsys)),
                urlFactory = get(),
                deviceInfoCollector = get(),
                contactTokenHandler = get(),
                sdkEventManager = get(),
                eventsDao = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                sdkLogger = get { parametersOf(DeviceClient::class.simpleName) }
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.RemoteConfig)) {
            RemoteConfigClient(
                networkClient = get(named(NetworkClientTypes.Generic)),
                urlFactoryApi = get(),
                sdkEventManager = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                eventsDao = get(),
                crypto = get(),
                json = get(),
                sdkLogger = get { parametersOf(RemoteConfigClient::class.simpleName) },
                remoteConfigResponseHandler = get()
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.Config)) {
            ConfigClient(
                emarsysNetworkClient = get(named(NetworkClientTypes.Emarsys)),
                urlFactory = get(),
                sdkEventManager = get(),
                sessionContext = get(),
                sdkContext = get(),
                contactTokenHandler = get(),
                eventsDao = get(),
                json = get(),
                sdkLogger = get { parametersOf(ConfigClient::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application)),
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.Logging)) {
            LoggingClient(
                genericNetworkClient = get(named(NetworkClientTypes.Generic)),
                urlFactory = get(),
                sdkEventManager = get(),
                json = get(),
                sdkLogger = get { parametersOf(LoggingClient::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                deviceInfoCollector = get(),
                eventsDao = get(),
                batchSize = 10
            )
        }
    }
}