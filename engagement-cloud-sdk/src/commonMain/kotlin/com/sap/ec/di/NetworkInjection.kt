package com.sap.ec.di

import com.sap.ec.core.networking.clients.GenericNetworkClient
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.networking.ECClient
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.config.ConfigClient
import com.sap.ec.networking.clients.device.DeviceClient
import com.sap.ec.networking.clients.embedded.messaging.EmbeddedMessagingClient
import com.sap.ec.networking.clients.error.ClientExceptionHandler
import com.sap.ec.networking.clients.error.DefaultClientExceptionHandler
import com.sap.ec.networking.clients.event.EventClient
import com.sap.ec.networking.clients.logging.LoggingClient
import com.sap.ec.networking.clients.remoteConfig.RemoteConfigClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object NetworkInjection {
    val networkModules = module {
        single<HttpClient> {
            HttpClient {
                install(HttpRequestRetry)
            }
        }
        single<NetworkClientApi>(named(NetworkClientTypes.Generic)) {
            GenericNetworkClient(
                client = get<HttpClient>(),
                sdkLogger = get { parametersOf(GenericNetworkClient::class.simpleName) },
            )
        }
        single<NetworkClientApi>(named(NetworkClientTypes.EC)) {
            ECClient(
                networkClient = get<NetworkClientApi>(
                    named(NetworkClientTypes.Generic)
                ),
                requestContext = get(),
                urlFactory = get(),
                json = get(),
                sdkLogger = get { parametersOf(ECClient::class.simpleName) },
                sdkEventDistributor = get()
            )
        }
        single<ClientExceptionHandler> {
            DefaultClientExceptionHandler(
                eventsDao = get(),
                get {
                    parametersOf(DefaultClientExceptionHandler::class.simpleName)
                })
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.Event)) {
            EventClient(
                ecNetworkClient = get(named(NetworkClientTypes.EC)),
                clientExceptionHandler = get(),
                urlFactory = get(),
                json = get(),
                eventActionFactory = get(),
                requestContext = get(),
                inAppConfigApi = get(),
                sdkEventManager = get(),
                eventsDao = get(),
                sdkLogger = get { parametersOf(EventClient::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                uuidProvider = get(),
                deviceInfoCollector = get(),
                pageLocationProvider = get()
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.EmbeddedMessaging)) {
            EmbeddedMessagingClient(
                sdkLogger = get { parametersOf(EmbeddedMessagingClient::class.simpleName) },
                sdkEventManager = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                embeddedMessagingRequestFactory = get(),
                ecNetworkClient = get(named(NetworkClientTypes.EC)),
                eventsDao = get(),
                clientExceptionHandler = get(),
                embeddedMessagingContext = get()
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.Device)) {
            DeviceClient(
                ecClient = get(named(NetworkClientTypes.EC)),
                clientExceptionHandler = get(),
                urlFactory = get(),
                deviceInfoCollector = get(),
                deviceInfoUpdater = get(),
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
                clientExceptionHandler = get(),
                urlFactoryApi = get(),
                sdkEventManager = get(),
                remoteConfigResponseHandler = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                eventsDao = get(),
                crypto = get(),
                json = get(),
                sdkLogger = get { parametersOf(RemoteConfigClient::class.simpleName) }
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.Config)) {
            ConfigClient(
                ecNetworkClient = get(named(NetworkClientTypes.EC)),
                clientExceptionHandler = get(),
                urlFactory = get(),
                sdkEventManager = get(),
                sdkContext = get(),
                contactTokenHandler = get(),
                followUpChangeAppCodeOrganizer = get(),
                eventsDao = get(),
                sdkLogger = get { parametersOf(ConfigClient::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application)),
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.Logging)) {
            LoggingClient(
                genericNetworkClient = get(named(NetworkClientTypes.Generic)),
                clientExceptionHandler = get(),
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