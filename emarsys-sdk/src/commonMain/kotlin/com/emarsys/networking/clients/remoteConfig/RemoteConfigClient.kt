package com.emarsys.networking.clients.remoteConfig

import com.emarsys.core.crypto.Crypto
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.remoteConfig.RemoteConfigResponse
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.UrlFactoryApi
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class RemoteConfigClient(
    private val networkClient: NetworkClientApi,
    private val urlFactoryApi: UrlFactoryApi,
    private val crypto: Crypto,
    private val json: Json
) : RemoteConfigClientApi {
    override suspend fun fetchRemoteConfig(): RemoteConfigResponse? {
        val toBeConfigBytes = fetchConfig()
        val toBeSignatureBytes = fetchSignature()
        val config = toBeConfigBytes.await()
        val verified = crypto.verify(config, toBeSignatureBytes.await())
        return if (verified) {
            json.decodeFromString(config.decodeToString())
        } else {
            null
        }
    }

    private suspend fun fetchConfig(): Deferred<ByteArray> = coroutineScope {
        async {
            val request =
                UrlRequest(urlFactoryApi.create(EmarsysUrlType.REMOTE_CONFIG), HttpMethod.Get)
            networkClient.send(request).bodyAsText.encodeToByteArray()
        }
    }

    private suspend fun fetchSignature(): Deferred<ByteArray> = coroutineScope {
        async {
            val request = UrlRequest(
                urlFactoryApi.create(EmarsysUrlType.REMOTE_CONFIG_SIGNATURE),
                HttpMethod.Get
            )
            networkClient.send(request).bodyAsText.encodeToByteArray()
        }
    }

}