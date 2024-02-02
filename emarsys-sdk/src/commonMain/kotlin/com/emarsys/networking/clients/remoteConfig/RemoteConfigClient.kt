package com.emarsys.networking.clients.remoteConfig

import com.emarsys.core.crypto.CryptoApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.remoteConfig.RemoteConfigResponse
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class RemoteConfigClient(
    private val networkClient: NetworkClientApi,
    private val urlFactoryApi: UrlFactoryApi,
    private val crypto: CryptoApi,
    private val json: Json
) : RemoteConfigClientApi {
    override suspend fun fetchRemoteConfig(): RemoteConfigResponse? {
        val toBeConfigBytes = fetchConfig()
        val toBeSignatureBytes = fetchSignature()
        val config = toBeConfigBytes.await()
        val verified = crypto.verify(config, toBeSignatureBytes.await())
        return if (verified) {
            json.decodeFromString(config)
        } else {
            null
        }
    }

    private suspend fun fetchConfig(): Deferred<String> = coroutineScope {
        async {
            val request =
                UrlRequest(urlFactoryApi.create(EmarsysUrlType.REMOTE_CONFIG), HttpMethod.Get)
            networkClient.send(request).bodyAsText
        }
    }

    private suspend fun fetchSignature(): Deferred<String> = coroutineScope {
        async {
            val request = UrlRequest(
                urlFactoryApi.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE),
                HttpMethod.Get
            )
            networkClient.send(request).bodyAsText
        }
    }

}