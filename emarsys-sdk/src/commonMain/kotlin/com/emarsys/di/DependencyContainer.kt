package com.emarsys.di

import com.emarsys.api.contact.Contact
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.contact.ContactContext
import com.emarsys.api.contact.ContactGatherer
import com.emarsys.api.contact.ContactInternal
import com.emarsys.api.contact.LoggingContact
import com.emarsys.api.event.EventTracker
import com.emarsys.api.event.EventTrackerApi
import com.emarsys.api.event.EventTrackerContext
import com.emarsys.api.event.EventTrackerGatherer
import com.emarsys.api.event.EventTrackerInternal
import com.emarsys.api.event.LoggingEventTracker
import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContext
import com.emarsys.api.push.PushGatherer
import com.emarsys.api.push.PushInstance
import com.emarsys.api.push.PushInternal
import com.emarsys.context.SdkContext
import com.emarsys.core.DefaultUrls
import com.emarsys.core.DefaultUrlsApi
import com.emarsys.core.channel.DeviceEventChannel
import com.emarsys.core.channel.DeviceEventChannelApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.state.StateMachine
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StorageApi
import com.emarsys.networking.EmarsysClient
import com.emarsys.networking.clients.contact.ContactClient
import com.emarsys.networking.clients.device.DeviceClient
import com.emarsys.networking.clients.device.DeviceClientApi
import com.emarsys.networking.clients.event.EventClient
import com.emarsys.networking.clients.event.EventClientApi
import com.emarsys.networking.clients.event.model.Event
import com.emarsys.networking.clients.push.PushClient
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.providers.Provider
import com.emarsys.providers.TimestampProvider
import com.emarsys.providers.UUIDProvider
import com.emarsys.session.SessionContext
import com.emarsys.setup.SetupOrganizer
import com.emarsys.setup.SetupOrganizerApi
import com.emarsys.setup.states.CollectDeviceInfoState
import com.emarsys.setup.states.RegisterClientState
import com.emarsys.setup.states.RegisterPushTokenState
import com.emarsys.url.UrlFactory
import com.emarsys.url.UrlFactoryApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

class DependencyContainer : DependencyContainerApi {

    private val platformContext: PlatformContext = CommonPlatformContext()

    private val dependencyCreator: DependencyCreator = PlatformDependencyCreator(platformContext)

    private val json: Json by lazy {
        Json {
            encodeDefaults = true
        }
    }

    private val stringStorage: StorageApi<String> by lazy { dependencyCreator.createStorage() }

    val storage: Storage by lazy { Storage(stringStorage, json) }

    override val uuidProvider: Provider<String> by lazy { UUIDProvider() }

    private val deviceInfoCollector: DeviceInfoCollectorApi by lazy {
        dependencyCreator.createDeviceInfoCollector(
            uuidProvider
        )
    }

    private val sdkLogger: SdkLogger by lazy { SdkLogger() }

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
        SdkContext(sdkDispatcher)
    }

    private val sessionContext: SessionContext by lazy {
        SessionContext(clientState = null, deviceEventState = null)
    }

    private val urlFactory: UrlFactoryApi by lazy {
        UrlFactory(sdkContext, defaultUrls)
    }


    private val timestampProvider: Provider<Instant> by lazy {
        TimestampProvider()
    }

    private val emarsysClient: NetworkClientApi by lazy {
        EmarsysClient(genericNetworkClient, sessionContext, timestampProvider, urlFactory, json)
    }

    private val deviceClient: DeviceClientApi by lazy {
        DeviceClient(emarsysClient, urlFactory, deviceInfoCollector, sessionContext)
    }

    private val eventClient: EventClientApi by lazy {
        EventClient(
            emarsysClient,
            urlFactory,
            json,
            deviceEventChannel,
            sessionContext,
            sdkContext,
            sdkDispatcher
        )
    }

    private val pushInternal: PushInstance by lazy {
        PushInternal(pushClient, stringStorage)
    }

    override val pushApi: PushApi by lazy {
        val pushContext = PushContext()
        val loggingPush = LoggingPush(sdkLogger)
        val pushGatherer = PushGatherer(pushContext)
        Push(loggingPush, pushGatherer, pushInternal, sdkContext)
    }
    private val pushClient: PushClientApi by lazy {
        PushClient(emarsysClient, urlFactory, json)
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

    override val setupOrganizerApi: SetupOrganizerApi by lazy {
        val collectDeviceInfoState = CollectDeviceInfoState(deviceInfoCollector, sessionContext)
        val registerClientState = RegisterClientState(deviceClient)
        val registerPushTokenState = RegisterPushTokenState(pushClient, stringStorage)
        val platformInitState = dependencyCreator.createPlatformInitState(pushInternal, sdkDispatcher)
        val stateMachine =
            StateMachine(listOf(collectDeviceInfoState, platformInitState, registerClientState, registerPushTokenState))
        SetupOrganizer(stateMachine, sdkContext)
    }

    override val contactApi: ContactApi = run {
        val contactClient = ContactClient(emarsysClient, urlFactory, sdkContext, json)
        val contactContext = ContactContext()
        val loggingContact = LoggingContact(sdkLogger)
        val contactGatherer = ContactGatherer(contactContext)
        val contactInternal = ContactInternal(contactClient)
        Contact(loggingContact, contactGatherer, contactInternal, sdkContext)
    }

    override val eventTrackerApi: EventTrackerApi = run {
        val eventTrackerContext = EventTrackerContext()
        val loggingEvent = LoggingEventTracker(sdkLogger)
        val gathererEvent = EventTrackerGatherer(eventTrackerContext)
        val eventInternal = EventTrackerInternal(eventClient)
        EventTracker(loggingEvent, gathererEvent, eventInternal, sdkContext)
    }
}