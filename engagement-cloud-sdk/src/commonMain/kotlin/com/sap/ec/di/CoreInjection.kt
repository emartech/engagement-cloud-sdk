package com.sap.ec.di

import com.sap.ec.IframeBridgeV2
import com.sap.ec.api.setup.Setup
import com.sap.ec.api.setup.SetupApi
import com.sap.ec.context.DefaultUrls
import com.sap.ec.context.DefaultUrlsApi
import com.sap.ec.context.SdkContext
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributor
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.channel.SdkEventEmitterApi
import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.crypto.Crypto
import com.sap.ec.core.crypto.CryptoApi
import com.sap.ec.core.device.DeviceInfoUpdater
import com.sap.ec.core.device.DeviceInfoUpdaterApi
import com.sap.ec.core.log.ConsoleLogger
import com.sap.ec.core.log.ConsoleLoggerApi
import com.sap.ec.core.log.LogEventRegistry
import com.sap.ec.core.log.LogEventRegistryApi
import com.sap.ec.core.log.LogLevel
import com.sap.ec.core.log.Logger
import com.sap.ec.core.log.RemoteLogger
import com.sap.ec.core.log.RemoteLoggerApi
import com.sap.ec.core.log.SdkLogger
import com.sap.ec.core.networking.UserAgentProvider
import com.sap.ec.core.networking.UserAgentProviderApi
import com.sap.ec.core.networking.context.RequestContext
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.providers.DoubleProvider
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.RandomProvider
import com.sap.ec.core.providers.TimestampProvider
import com.sap.ec.core.providers.TimezoneProvider
import com.sap.ec.core.providers.TimezoneProviderApi
import com.sap.ec.core.providers.UUIDProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.core.providers.sdkversion.SdkVersionProvider
import com.sap.ec.core.providers.sdkversion.SdkVersionProviderApi
import com.sap.ec.core.session.SessionContext
import com.sap.ec.core.storage.Storage
import com.sap.ec.core.storage.StorageApi
import com.sap.ec.core.storage.TypedStorage
import com.sap.ec.core.storage.TypedStorageApi
import com.sap.ec.core.url.UrlFactory
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.core.util.Downloader
import com.sap.ec.core.util.DownloaderApi
import com.sap.ec.mobileengage.action.EventActionFactory
import com.sap.ec.mobileengage.action.EventActionFactoryApi
import com.sap.ec.mobileengage.embeddedmessaging.networking.EmbeddedMessagesRequestFactory
import com.sap.ec.mobileengage.embeddedmessaging.networking.EmbeddedMessagingRequestFactoryApi
import com.sap.ec.util.JsonUtil
import com.sap.ec.watchdog.connection.ConnectionWatchDog
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
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
        single<LogEventRegistryApi> {
            LogEventRegistry()
        }
        single<RemoteLoggerApi> {
            RemoteLogger(
                logEventRegistry = get(),
                sdkContext = get()
            )
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
        single<SdkVersionProviderApi> { SdkVersionProvider() }
        single<TypedStorageApi> {
            TypedStorage(
                stringStorage = get(),
                json = get(),
                sdkLogger = get { parametersOf(TypedStorage::class.simpleName) }
            )
        }
        single<Json> { JsonUtil.json }
        single<StorageApi> { Storage(stringStorage = get(), json = get()) }
        single<DeviceInfoUpdaterApi> { DeviceInfoUpdater(stringStorage = get()) }
        singleOf(::UserAgentProvider) { bind<UserAgentProviderApi>() }
        single<DefaultUrlsApi> {
            DefaultUrls(
                "https://me-client.eservice.emarsys.net",
                "https://mobile-events.eservice.emarsys.net",
                "https://deep-link.eservice.emarsys.net",
                "https://mobile-sdk-config.gservice.emarsys.net",
                "https://log-dealer.gservice.emarsys.net",
                "https://embedded-messaging.gservice.emarsys.net/embedded-messaging/api",
                IframeBridgeV2.IFRAME_BRIDGE_V2
            )
        }
        single<SdkEventDistributor> {
            SdkEventDistributor(
                connectionStatus = get<ConnectionWatchDog>().isOnline,
                sdkContext = get(),
                eventsDao = get(),
                logEventRegistry = get(),
                applicationScope = get(named(CoroutineScopeTypes.Application)),
                sdkLogger = get { parametersOf(SdkEventDistributor::class.simpleName) }
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
    Generic, EC
}

enum class EventBasedClientTypes {
    Device, Config, DeepLink, Contact, Event, Push, RemoteConfig, Logging, Reregistration, EmbeddedMessaging
}

enum class EventFlowTypes {
    Public
}

enum class SdkConfigStoreTypes {
    EC, Android, Web
}

object PersistentListIds {
    const val PUSH_CONTEXT_PERSISTENT_ID = "pushContextPersistentId"
    const val INAPP_CONTEXT_PERSISTENT_ID = "inAppContextPersistentId"
    const val CONFIG_CONTEXT_PERSISTENT_ID = "configContextPersistentId"
    const val CONTACT_CONTEXT_PERSISTENT_ID = "contactContextPersistentId"
    const val EVENT_TRACKER_CONTEXT_PERSISTENT_ID = "eventTrackerContextPersistentId"

}