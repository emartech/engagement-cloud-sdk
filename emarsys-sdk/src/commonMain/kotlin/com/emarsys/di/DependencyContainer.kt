package com.emarsys.di

import com.emarsys.api.contact.Contact
import com.emarsys.api.contact.ContactApi
import com.emarsys.api.contact.ContactContext
import com.emarsys.api.contact.ContactInternal
import com.emarsys.api.contact.GathererContact
import com.emarsys.api.contact.LoggingContact
import com.emarsys.api.push.GathererPush
import com.emarsys.api.push.LoggingPush
import com.emarsys.api.push.Push
import com.emarsys.api.push.PushApi
import com.emarsys.api.push.PushContext
import com.emarsys.api.push.PushInternal
import com.emarsys.context.SdkContext
import com.emarsys.core.DefaultUrls
import com.emarsys.core.DefaultUrlsApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.networking.clients.GenericNetworkClient
import com.emarsys.core.networking.clients.push.PushClient
import com.emarsys.core.networking.clients.push.PushClientApi
import com.emarsys.core.storage.Storage
import com.emarsys.core.storage.StorageApi
import com.emarsys.networking.ktor.plugin.EmarsysAuthPlugin
import com.emarsys.providers.Provider
import com.emarsys.providers.UUIDProvider
import com.emarsys.session.SessionContext
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.FactoryApi
import com.emarsys.url.UrlFactory
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class DependencyContainer : DependencyContainerApi {

    private val platformContext: PlatformContext = CommonPlatformContext()

    private val dependencyCreator: DependencyCreator = PlatformDependencyCreator(platformContext)

    private val json: Json by lazy { Json }

    private val stringStorage: StorageApi<String> by lazy { dependencyCreator.createStorage() }

    val storage: Storage by lazy { Storage(stringStorage, json) }

    override val uuidProvider: Provider<String> by lazy { UUIDProvider() }

    val deviceInfoCollector: DeviceInfoCollectorApi by lazy { dependencyCreator.createDeviceInfoCollector(uuidProvider) }

    val sdkLogger: SdkLogger by lazy { SdkLogger() }

    val sdkContext: SdkContext by lazy {
        SdkContext()
    }

    val sessionContext: SessionContext by lazy {
        SessionContext(clientState = null)
    }

    val urlFactory: FactoryApi<EmarsysUrlType, String> by lazy {
        UrlFactory(sdkContext, defaultUrls)
    }

    override val contactApi: ContactApi by lazy {
        val contactContext = ContactContext()
        val loggingContact = LoggingContact(sdkLogger)
        val gathererContact = GathererContact(contactContext)
        val contactInternal = ContactInternal()
        Contact(loggingContact, gathererContact, contactInternal, sdkContext)
    }
    override val pushApi: PushApi by lazy {
        val pushContext = PushContext()
        val loggingPush = LoggingPush(sdkLogger)
        val gathererPush = GathererPush(pushContext)
        val pushInternal = PushInternal(pushClient)
        Push(loggingPush, gathererPush, pushInternal, sdkContext)
    }
    val pushClient: PushClientApi by lazy {
        PushClient(genericNetworkClient, urlFactory, json)
    }

    val defaultUrls: DefaultUrlsApi by lazy {
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
    val genericNetworkClient: GenericNetworkClient by lazy {
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json()
            }
            install(HttpRequestRetry)
            install(EmarsysAuthPlugin) {
                sessionContext = this@DependencyContainer.sessionContext
            }
        }
        GenericNetworkClient(httpClient)
    }
}