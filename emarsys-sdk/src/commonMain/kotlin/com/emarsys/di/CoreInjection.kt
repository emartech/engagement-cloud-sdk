package com.emarsys.di

import com.emarsys.api.setup.Setup
import com.emarsys.api.setup.SetupApi
import com.emarsys.context.DefaultUrls
import com.emarsys.context.DefaultUrlsApi
import com.emarsys.context.SdkContext
import com.emarsys.context.SdkContextApi
import com.emarsys.core.channel.SdkEventDistributor
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.channel.SdkEventEmitterApi
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.crypto.CryptoApi
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.ConsoleLoggerApi
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.Logger
import com.emarsys.core.log.RemoteLogger
import com.emarsys.core.log.RemoteLoggerApi
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.networking.UserAgentProvider
import com.emarsys.core.networking.UserAgentProviderApi
import com.emarsys.core.networking.context.RequestContext
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.providers.DoubleProvider
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.providers.RandomProvider
import com.emarsys.core.providers.TimestampProvider
import com.emarsys.core.providers.TimezoneProvider
import com.emarsys.core.providers.TimezoneProviderApi
import com.emarsys.core.providers.UUIDProvider
import com.emarsys.core.providers.UuidProviderApi
import com.emarsys.core.session.SessionContext
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StorageApi
import com.emarsys.core.storage.TypedStorage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.UrlFactory
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.core.util.Downloader
import com.emarsys.core.util.DownloaderApi
import com.emarsys.mobileengage.action.EventActionFactory
import com.emarsys.mobileengage.action.EventActionFactoryApi
import com.emarsys.mobileengage.embedded.messages.EmbeddedMessagesRequestFactory
import com.emarsys.mobileengage.embedded.messages.EmbeddedMessagingRequestFactoryApi
import com.emarsys.util.JsonUtil
import com.emarsys.watchdog.connection.ConnectionWatchDog
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

object CoreInjection {
    private const val PUBLIC_KEY =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELjWEUIBX9zlm1OI4gF1hMCBLzpaBwgs9HlmSIBAqP4MDGy4ibOOV3FVDrnAY0Q34LZTbPBlp3gRNZJ19UoSy2Q=="

    val coreModules = module {
        single<CoroutineDispatcher>(named(DispatcherTypes.Sdk)) { Dispatchers.Default }
        single<CoroutineDispatcher>(named(DispatcherTypes.Main)) { Dispatchers.Main }
        single<CoroutineScope>(named(CoroutineScopeTypes.Application)) {
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }
        single<ConsoleLoggerApi> {
            ConsoleLogger()
        }
        single<RemoteLoggerApi> {
            RemoteLogger()
        }
        factory<Logger> { (loggerName: String) ->
            SdkLogger(
                loggerName,
                consoleLogger = get(),
                remoteLogger = get(),
                sdkContext = get()
            )
        }
        singleOf(::TimestampProvider) { bind<InstantProvider>() }
        singleOf(::UUIDProvider) { bind<UuidProviderApi>() }
        singleOf(::TimezoneProvider) { bind<TimezoneProviderApi>() }
        singleOf(::RandomProvider) { bind<DoubleProvider>() }
        single<TypedStorageApi> {
            TypedStorage(
                stringStorage = get(),
                json = get(),
                sdkLogger = get { parametersOf(TypedStorage::class.simpleName) }
            )
        }
        single<Json> { JsonUtil.json }
        single<StorageApi> { Storage(stringStorage = get(), json = get()) }
        singleOf(::UserAgentProvider) { bind<UserAgentProviderApi>() }
        single<DefaultUrlsApi> {
            DefaultUrls(
                "https://me-client.eservice.emarsys.net",
                "https://mobile-events.eservice.emarsys.net",
                "https://deep-link.eservice.emarsys.net",
                "https://mobile-sdk-config.gservice.emarsys.net",
                "https://log-dealer.gservice.emarsys.net",
                "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/fake-api"
            )
        }
        single<SdkEventDistributor> {
            SdkEventDistributor(
                get<ConnectionWatchDog>().isOnline,
                sdkContext = get(),
                eventsDao = get(),
                sdkLogger = get { parametersOf(SdkEventDistributor::class.simpleName) },
            )
        } binds arrayOf(
            SdkEventDistributorApi::class,
            SdkEventEmitterApi::class,
            SdkEventManagerApi::class
        )
        single<SetupApi> {
            Setup(
                get(),
                get(),
                get(),
                get { parametersOf(Setup::class.simpleName) })
        }
        single<SdkContextApi> {
            SdkContext(
                sdkDispatcher = get(named(DispatcherTypes.Sdk)),
                mainDispatcher = get(named(DispatcherTypes.Main)),
                defaultUrls = get(),
                remoteLogLevel = LogLevel.Error,
                features = mutableSetOf(),
                logBreadcrumbsQueueSize = 10
            )
        }
        single<HttpClient> {
            HttpClient {
                install(ContentNegotiation) {
                    json(get<Json>())
                }
                install(HttpRequestRetry)
            }
        }
        single<DownloaderApi> {
            Downloader(
                client = get<HttpClient>(),
                fileCache = get(),
                logger = get<Logger> { parametersOf(Downloader::class.simpleName) },
            )
        }
        single<EventActionFactoryApi> {
            EventActionFactory(
                sdkEventDistributor = get(),
                permissionHandler = get(),
                externalUrlOpener = get(),
                clipboardHandler = get(),
                sdkLogger = get<Logger> { parametersOf(EventActionFactory::class.simpleName) },
            )
        }
        single<RequestContextApi> { RequestContext() }
        single<SessionContext> { SessionContext() }
        singleOf(::UrlFactory) { bind<UrlFactoryApi>() }
        singleOf(::EmbeddedMessagesRequestFactory) { bind<EmbeddedMessagingRequestFactoryApi>() }
        single<CryptoApi> {
            Crypto(
                logger = get<Logger> { parametersOf(Crypto::class.simpleName) },
                PUBLIC_KEY
            )
        }
    }
}

enum class DispatcherTypes {
    Sdk, Main
}

enum class CoroutineScopeTypes {
    Application
}

enum class PersistentListTypes {
    PushCall, InAppCall, ConfigCall, ContactCall, EventTrackerCall
}

enum class NetworkClientTypes {
    Generic, Emarsys
}

enum class EventBasedClientTypes {
    Device, Config, DeepLink, Contact, Event, Push, RemoteConfig, Logging, Reregistration, EmbeddedMessaging
}

enum class EventFlowTypes {
    Public
}

enum class SdkConfigStoreTypes {
    Emarsys, Android, Web
}

object PersistentListIds {
    const val PUSH_CONTEXT_PERSISTENT_ID = "pushContextPersistentId"
    const val INAPP_CONTEXT_PERSISTENT_ID = "inAppContextPersistentId"
    const val CONFIG_CONTEXT_PERSISTENT_ID = "configContextPersistentId"
    const val CONTACT_CONTEXT_PERSISTENT_ID = "contactContextPersistentId"
    const val EVENT_TRACKER_CONTEXT_PERSISTENT_ID = "eventTrackerContextPersistentId"

}