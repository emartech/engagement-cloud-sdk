package com.emarsys.di

import com.emarsys.core.device.DeviceInfoCollector
import com.emarsys.networking.GenericNetworkClient
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import com.emarsys.core.storage.StorageApi

class DependencyContainer : DependencyContainerApi {

    private val platformContext: PlatformContext = CommonPlatformContext()

    private val dependencyCreator: DependencyCreator = PlatformDependencyCreator(platformContext)

    val storage: StorageApi by lazy { dependencyCreator.createStorage() }

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