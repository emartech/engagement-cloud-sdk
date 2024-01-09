package com.emarsys.di

import SdkContext
import com.emarsys.api.contact.*
import com.emarsys.api.push.*
import com.emarsys.clients.push.PushClient
import com.emarsys.clients.push.PushClientApi
import com.emarsys.core.DefaultUrls
import com.emarsys.core.DefaultUrlsApi
import com.emarsys.core.device.DeviceInfoCollectorApi
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.storage.Storage
import com.emarsys.core.networking.GenericNetworkClient
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import com.emarsys.core.storage.StorageApi
import com.emarsys.networking.ktor.plugin.EmarsysAuthPlugin
import com.emarsys.providers.Provider
import com.emarsys.providers.UUIDProvider
import com.emarsys.session.SessionContext
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
        SessionContext()
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
        PushClient(genericNetworkClient, sdkContext, defaultUrls, json)
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