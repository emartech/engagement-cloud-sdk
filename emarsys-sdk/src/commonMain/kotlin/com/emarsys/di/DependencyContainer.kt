package com.emarsys.di

import SdkContext
import com.emarsys.api.contact.*
import com.emarsys.core.log.SdkLogger
import com.emarsys.core.storage.Storage
import com.emarsys.networking.GenericNetworkClient
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import com.emarsys.core.storage.StorageApi
import com.emarsys.providers.Provider
import com.emarsys.providers.UUIDProvider
import kotlinx.serialization.json.Json

class DependencyContainer : DependencyContainerApi {

    private val platformContext: PlatformContext = CommonPlatformContext()

    private val dependencyCreator: DependencyCreator = PlatformDependencyCreator(platformContext)

    private val json: Json by lazy { Json }

    private val stringStorage: StorageApi<String> by lazy { dependencyCreator.createStorage() }

    val storage: Storage by lazy { Storage(stringStorage, json) }

    val uuidProvider: Provider<String> by lazy { UUIDProvider() }

    val sdkLogger: SdkLogger by lazy { SdkLogger() }

    val sdkContext: SdkContext by lazy {
        SdkContext()
    }

    override val contactApi: ContactApi by lazy {
        val contactContext = ContactContext()
        val loggingContact = LoggingContact(sdkLogger)
        val gathererContact = GathererContact(contactContext)
        val contactInternal = ContactInternal()
        Contact(loggingContact, gathererContact, contactInternal, sdkContext)
    }

    val genericNetworkClient: GenericNetworkClient by lazy {
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json()
            }
            install(HttpRequestRetry)
        }
        GenericNetworkClient(httpClient)
    }
}