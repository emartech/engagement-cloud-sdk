package com.emarsys.di

import PlatformContext
import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.core.storage.Storage
import com.emarsys.networking.GenericNetworkClient
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

class DependencyContainer : DependencyContainerApi {

    val platformContext: PlatformContext = CommonPlatformContext()

    val dependencyCreator: DependencyCreator = PlatformDependencyCreator(platformContext)

    val storage: Storage by lazy { dependencyCreator.createStringStorage() }

    val deviceInfoCollector: DeviceInfoCollector by lazy { dependencyCreator.createDeviceInfoCollector() }

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