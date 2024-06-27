package com.emarsys.di

import EventTrackerApi
import com.emarsys.api.AppEvent
import com.emarsys.api.config.Config
import com.emarsys.api.config.ConfigApi
import com.emarsys.api.config.ConfigCall
import com.emarsys.api.config.ConfigContext
import com.emarsys.api.config.ConfigInternal
import com.emarsys.api.config.GathererConfig
import com.emarsys.api.config.LoggingConfig
import com.emarsys.api.contact.Contact
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.contact.ContactCall
import com.emarsys.api.contact.ContactContext
import com.emarsys.api.contact.ContactGatherer
import com.emarsys.api.contact.ContactInternal
import com.emarsys.api.contact.LoggingContact
import com.emarsys.api.event.EventTracker
import com.emarsys.api.event.EventTrackerCall
import com.emarsys.api.event.EventTrackerContext
import com.emarsys.api.event.EventTrackerGatherer
import com.emarsys.api.event.EventTrackerInternal
import com.emarsys.api.event.LoggingEventTracker
import com.emarsys.api.generic.ApiContext
import com.emarsys.api.geofence.GathererGeofenceTracker
import com.emarsys.api.geofence.GeofenceTracker
import com.emarsys.api.geofence.GeofenceTrackerApi
import com.emarsys.api.geofence.GeofenceTrackerCall
import com.emarsys.api.geofence.GeofenceTrackerContext
import com.emarsys.api.geofence.GeofenceTrackerInternal
import com.emarsys.api.geofence.LoggingGeofenceTracker
import com.emarsys.api.inapp.GathererInApp
import com.emarsys.api.inapp.InApp
import com.emarsys.api.inapp.InAppApi
import com.emarsys.api.inapp.InAppCall
import com.emarsys.api.inapp.InAppContext
import com.emarsys.api.inapp.InAppInternal
import com.emarsys.api.inapp.LoggingInApp
import com.emarsys.api.inbox.GathererInbox
import com.emarsys.api.inbox.Inbox
import com.emarsys.api.inbox.InboxApi
import com.emarsys.api.inbox.InboxCall
import com.emarsys.api.inbox.InboxContext
import com.emarsys.api.inbox.InboxInternal
import com.emarsys.api.inbox.LoggingInbox
import com.emarsys.api.oneventaction.OnEventAction
import com.emarsys.api.oneventaction.OnEventActionApi
import com.emarsys.api.oneventaction.OnEventActionInternal
import com.emarsys.api.predict.GathererPredict
import com.emarsys.api.predict.LoggingPredict
import com.emarsys.api.predict.Predict
import com.emarsys.api.predict.PredictApi
import com.emarsys.api.predict.PredictCall
import com.emarsys.api.predict.PredictContext
import com.emarsys.api.predict.PredictInternal
import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushCall
import com.emarsys.api.push.PushContext
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.context.DefaultUrls
import com.emarsys.context.DefaultUrlsApi
import com.emarsys.context.SdkContext
import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.core.channel.DeviceEventChannel
import com.emarsys.core.channel.DeviceEventChannelApi
import com.emarsys.core.collections.persistentListOf
import com.emarsys.core.crypto.Crypto
import com.emarsys.core.crypto.CryptoApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.ConsoleLogger
import com.emarsys.core.log.LogLevel
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.message.MsgHub
import com.emarsys.core.message.MsgHubApi
import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.permission.PermissionHandlerApi
import com.emarsys.core.providers.Provider
import com.emarsys.core.providers.RandomProvider
import com.emarsys.core.providers.TimestampProvider
import com.emarsys.core.providers.TimezoneProvider
import com.emarsys.core.providers.UUIDProvider
import com.emarsys.core.session.SessionContext
import com.emarsys.core.state.StateMachine
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.core.url.UrlFactory
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.mobileengage.action.ActionFactory
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.session.MobileEngageSession
import com.emarsys.mobileengage.session.Session
import com.emarsys.networking.EmarsysClient
import com.emarsys.networking.clients.contact.ContactClient
import com.emarsys.networking.clients.contact.ContactClientApi
import com.emarsys.networking.clients.contact.ContactTokenHandler
import com.emarsys.networking.clients.contact.ContactTokenHandlerApi
import com.emarsys.networking.clients.device.DeviceClient
import com.emarsys.networking.clients.device.DeviceClientApi
import com.emarsys.networking.clients.event.EventClient
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.push.PushClient
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.networking.clients.remoteConfig.RemoteConfigClient
import com.emarsys.remoteConfig.RemoteConfigHandler
import com.emarsys.remoteConfig.RemoteConfigHandlerApi
import com.emarsys.setup.SetupOrganizer
import com.emarsys.setup.SetupOrganizerApi
import com.emarsys.setup.states.AppStartState
import com.emarsys.setup.states.ApplyRemoteConfigState
import com.emarsys.setup.states.CollectDeviceInfoState
import com.emarsys.setup.states.LinkAnonymousContactState
import com.emarsys.setup.states.RegisterClientState
import com.emarsys.setup.states.RegisterPushTokenState
import com.emarsys.watchdog.connection.ConnectionWatchDog
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

class DependencyContainer : DependencyContainerApi {
    private companion object {
        const val PUBLIC_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAELjWEUIBX9zlm1OI4gF1hMCBLzpaBwgs9HlmSIBAqP4MDGy4ibOOV3FVDrnAY0Q34LZTbPBlp3gRNZJ19UoSy2Q=="
    }

    private val platformContext: PlatformContext = CommonPlatformContext()

    private val sdkLogger: SdkLogger = SdkLogger(ConsoleLogger())

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    override val uuidProvider: Provider<String> by lazy { UUIDProvider() }

    private val dependencyCreator: DependencyCreator =
        PlatformDependencyCreator(platformContext, uuidProvider, sdkLogger, json)

    private val stringStorage: TypedStorageApi<String?> by lazy { dependencyCreator.createStorage() }

    private val permissionHandler: PermissionHandlerApi by lazy { dependencyCreator.createPermissionHandler() }

    private val badgeCountHandler: BadgeCountHandlerApi by lazy { dependencyCreator.createBadgeCountHandler() }

    private val externalUrlOpener: ExternalUrlOpenerApi by lazy { dependencyCreator.createExternalUrlOpener() }

    private val msgHub: MsgHubApi by lazy { MsgHub(sdkDispatcher) }

    private val actionFactory: ActionFactoryApi<ActionModel> by lazy {
        ActionFactory(
            onEventActionInternal,
            deviceEventChannel,
            permissionHandler,
            badgeCountHandler,
            externalUrlOpener,
            msgHub
        )
    }

    private val onEventActionFactory: ActionFactoryApi<BasicActionModel> by lazy {
        ActionFactory(
            onEventActionInternal,
            deviceEventChannel,
            permissionHandler,
            badgeCountHandler,
            externalUrlOpener,
            msgHub
        )
    }

    val storage: Storage by lazy { Storage(stringStorage, json) }

    override val timezoneProvider: Provider<String> by lazy { TimezoneProvider(timestampProvider) }

    private val deviceInfoCollector: DeviceInfoCollectorApi by lazy {
        dependencyCreator.createDeviceInfoCollector(
            timezoneProvider
        )
    }

    val sdkDispatcher: CoroutineDispatcher by lazy {
        Dispatchers.Default
    }

    private val eventChannel: Channel<Event> by lazy {
        Channel()
    }

    private val deviceEventChannel: DeviceEventChannelApi by lazy {
        DeviceEventChannel(eventChannel)
    }

    val sdkContext: SdkContext by lazy {
        SdkContext(sdkDispatcher, defaultUrls, LogLevel.Error, mutableSetOf())
    }

    private val sessionContext: SessionContext by lazy {
        SessionContext(clientState = null, deviceEventState = null)
    }

    private val urlFactory: UrlFactoryApi by lazy {
        UrlFactory(sdkContext)
    }


    private val crypto: CryptoApi by lazy {
        Crypto(PUBLIC_KEY)
    }

    private val timestampProvider: Provider<Instant> by lazy {
        TimestampProvider()
    }

    private val emarsysClient: NetworkClientApi by lazy {
        EmarsysClient(genericNetworkClient, sessionContext, timestampProvider, urlFactory, json)
    }

    private val contactTokenHandler: ContactTokenHandlerApi by lazy {
        ContactTokenHandler(sessionContext)
    }

    private val deviceClient: DeviceClientApi by lazy {
        DeviceClient(emarsysClient, urlFactory, deviceInfoCollector, contactTokenHandler)
    }

    private val eventClient: EventClientApi by lazy {
        EventClient(
            emarsysClient,
            urlFactory,
            json,
            deviceEventChannel,
            onEventActionFactory,
            sessionContext,
            inAppContext,
            sdkDispatcher
        )
    }

    private val onEventActionInternal: OnEventActionInternal by lazy {
        OnEventActionInternal()
    }


    private val pushContext: ApiContext<PushCall> by lazy {
        PushContext(persistentListOf("pushContextPersistentId", storage, PushCall.serializer()))
    }

    val notificationEvents: MutableSharedFlow<AppEvent> by lazy {
        MutableSharedFlow(replay = 100)
    }
    private val pushInternal: PushInstance by lazy {
        PushInternal(pushClient, stringStorage, pushContext, notificationEvents)
    }

    override val inAppApi: InAppApi by lazy {
        val events = MutableSharedFlow<AppEvent>(replay = 100)

        val loggingInApp = LoggingInApp(sdkContext, sdkLogger)
        val gathererInApp = GathererInApp(inAppContext, events)
        val inAppInternal = InAppInternal(events)
        InApp(loggingInApp, gathererInApp, inAppInternal, sdkContext)
    }

    private val inAppContext = InAppContext(
        persistentListOf(
            "inAppContextPersistentId",
            storage,
            InAppCall.serializer()
        )
    )

    override val inboxApi: InboxApi by lazy {
        val loggingInbox = LoggingInbox(sdkLogger)
        val gathererInbox = GathererInbox(inboxContext)
        val inboxInternal = InboxInternal()
        Inbox(loggingInbox, gathererInbox, inboxInternal, sdkContext)
    }

    private val inboxContext = InboxContext(
        persistentListOf(
            "inboxContextPersistentId",
            storage,
            InboxCall.serializer()
        )
    )

    override val predictApi: PredictApi by lazy {
        val loggingPredict = LoggingPredict(sdkLogger)
        val gathererPredict = GathererPredict(predictContext)
        val predictInternal = PredictInternal()
        Predict(loggingPredict, gathererPredict, predictInternal, sdkContext)
    }

    private val predictContext = PredictContext(
        persistentListOf(
            "predictContextPersistentId",
            storage,
            PredictCall.serializer()
        )
    )

    override val geofenceTrackerApi: GeofenceTrackerApi by lazy {
        val geofenceEvents = MutableSharedFlow<AppEvent>(replay = 100)

        val loggingGeofenceTracker = LoggingGeofenceTracker(sdkContext, sdkLogger)
        val gathererGeofenceTracker =
            GathererGeofenceTracker(geofenceTrackerContext, geofenceEvents)
        val geofenceTrackerInternal = GeofenceTrackerInternal()

        GeofenceTracker(
            loggingGeofenceTracker,
            gathererGeofenceTracker,
            geofenceTrackerInternal,
            sdkContext
        )
    }

    private val geofenceTrackerContext = GeofenceTrackerContext(
        persistentListOf(
            "geofenceTrackerContextPersistentId",
            storage,
            GeofenceTrackerCall.serializer()
        )
    )

    override val onEventActionApi: OnEventActionApi by lazy {
        OnEventAction(onEventActionInternal)
    }
    override val configApi: ConfigApi by lazy {
        val loggingConfig = LoggingConfig(sdkLogger)
        val gathererConfig = GathererConfig(
            ConfigContext(
                persistentListOf(
                    "configContextPersistentId",
                    storage,
                    ConfigCall.serializer()
                )
            )
        )
        val configInternal = ConfigInternal()
        Config(loggingConfig, gathererConfig, configInternal, sdkContext, deviceInfoCollector)
    }

    override val pushApi: PushApi by lazy {
        val loggingPush = LoggingPush(sdkLogger, notificationEvents)
        val pushGatherer = PushGatherer(pushContext, stringStorage, notificationEvents)
        Push(loggingPush, pushGatherer, pushInternal, sdkContext)
    }

    private val pushClient: PushClientApi by lazy {
        PushClient(emarsysClient, urlFactory, json)
    }

    private val contactClient: ContactClientApi by lazy {
        ContactClient(emarsysClient, urlFactory, sdkContext, contactTokenHandler, json)
    }

    private val defaultUrls: DefaultUrlsApi by lazy {
        DefaultUrls(
            "https://me-client.eservice.emarsys.net",
            "https://mobile-events.eservice.emarsys.net",
            "https://recommender.scarabresearch.com/merchants",
            "https://deep-link.eservice.emarsys.net",
            "https://me-inbox.eservice.emarsys.net",
            "https://mobile-sdk-config.gservice.emarsys.net",
            "https://log-dealer.eservice.emarsys.net"
        )
    }

    private val genericNetworkClient: NetworkClientApi by lazy {
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json()
            }
            install(HttpRequestRetry)
        }
        GenericNetworkClient(httpClient)
    }
    override val remoteConfigHandler: RemoteConfigHandlerApi by lazy {
        RemoteConfigHandler(
            RemoteConfigClient(genericNetworkClient, urlFactory, crypto, json),
            deviceInfoCollector,
            sdkContext,
            RandomProvider()
        )
    }
    override val setupOrganizerApi: SetupOrganizerApi by lazy {
        val collectDeviceInfoState = CollectDeviceInfoState(deviceInfoCollector, sessionContext)
        val registerClientState = RegisterClientState(deviceClient)
        val registerPushTokenState = RegisterPushTokenState(pushClient, stringStorage)
        val platformInitState =
            dependencyCreator.createPlatformInitState(
                pushInternal,
                sdkDispatcher,
                sdkContext,
                actionFactory
            )
        val applyRemoteConfigState = ApplyRemoteConfigState(
            remoteConfigHandler
        )
        val linkAnonymousContactState = LinkAnonymousContactState(contactClient, sessionContext)
        val appStartState = AppStartState(eventClient, timestampProvider)
        val stateMachine =
            StateMachine(
                listOf(
                    collectDeviceInfoState,
                    applyRemoteConfigState,
                    platformInitState,
                    registerClientState,
                    registerPushTokenState,
                    linkAnonymousContactState,
                    appStartState
                )
            )
        SetupOrganizer(stateMachine, sdkContext)
    }

    override val contactApi: ContactApi by lazy {
        val contactClient =
            ContactClient(emarsysClient, urlFactory, sdkContext, contactTokenHandler, json)
        val contactContext =
            ContactContext(
                persistentListOf(
                    "contactContextPersistentId",
                    storage,
                    ContactCall.serializer()
                )
            )
        val loggingContact = LoggingContact(sdkLogger)
        val contactGatherer = ContactGatherer(contactContext)
        val contactInternal = ContactInternal(contactClient, contactContext)
        Contact(loggingContact, contactGatherer, contactInternal, sdkContext)
    }

    override val eventTrackerApi: EventTrackerApi by lazy {
        val eventTrackerContext = EventTrackerContext(
            persistentListOf(
                "eventTrackerContextPersistentId",
                storage,
                EventTrackerCall.serializer()
            )
        )
        val loggingEvent = LoggingEventTracker(sdkLogger)
        val gathererEvent = EventTrackerGatherer(eventTrackerContext, timestampProvider)
        val eventInternal =
            EventTrackerInternal(eventClient, eventTrackerContext, timestampProvider)
        EventTracker(loggingEvent, gathererEvent, eventInternal, sdkContext)
    }
    override val connectionWatchDog: ConnectionWatchDog by lazy {
        dependencyCreator.createConnectionWatchDog(sdkLogger)
    }

    override val lifecycleWatchDog: LifecycleWatchDog by lazy {
        dependencyCreator.createLifeCycleWatchDog()
    }

    override val mobileEngageSession: Session by lazy {
        MobileEngageSession(
            timestampProvider,
            uuidProvider,
            sessionContext,
            sdkContext,
            eventClient,
            sdkDispatcher,
            sdkLogger
        )
    }


    override suspend fun setup() {
        eventTrackerApi.registerOnContext()
        contactApi.registerOnContext()
        pushApi.registerOnContext()

        connectionWatchDog.register()
        lifecycleWatchDog.register()

        mobileEngageSession.subscribe(lifecycleWatchDog)
    }
}