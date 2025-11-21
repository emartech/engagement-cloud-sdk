package com.emarsys.di

import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.networking.EmarsysClient
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.config.ConfigClient
import com.emarsys.networking.clients.device.DeviceClient
import com.emarsys.networking.clients.embedded.messaging.EmbeddedMessagingClient
import com.emarsys.networking.clients.error.ClientExceptionHandler
import com.emarsys.networking.clients.error.DefaultClientExceptionHandler
import com.emarsys.networking.clients.event.EventClient
import com.emarsys.networking.clients.logging.LoggingClient
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

object NetworkInjection {
    val networkModules = module {
        single<HttpClient> {
            HttpClient {
                install(ContentNegotiation) {
                    get<Json>()
                }
                install(HttpRequestRetry)
            }
        }
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
                requestContext = get(),
                timestampProvider = get(),
                urlFactory = get(),
                json = get(),
                sdkLogger = get { parametersOf(EmarsysClient::class.simpleName) },
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
                emarsysNetworkClient = get(named(NetworkClientTypes.Emarsys)),
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
                uuidProvider = get()
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.EmbeddedMessaging)) {
            EmbeddedMessagingClient(
                emarsysNetworkClient = get(named(NetworkClientTypes.Emarsys)),
                clientExceptionHandler = get(),
                embeddedMessagingRequestFactory = get(),
                sdkEventManager = get(),
                eventsDao = get(),
                sdkLogger = get { parametersOf(EmbeddedMessagingClient::class.simpleName) },
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                timestampProvider = get(),
                embeddedMessagingContext = get()
            )
        }
        single<EventBasedClientApi>(named(EventBasedClientTypes.Device)) {
            DeviceClient(
                emarsysClient = get(named(NetworkClientTypes.Emarsys)),
                clientExceptionHandler = get(),
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
                emarsysNetworkClient = get(named(NetworkClientTypes.Emarsys)),
                clientExceptionHandler = get(),
                urlFactory = get(),
                sdkEventManager = get(),
                sdkContext = get(),
                contactTokenHandler = get(),
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