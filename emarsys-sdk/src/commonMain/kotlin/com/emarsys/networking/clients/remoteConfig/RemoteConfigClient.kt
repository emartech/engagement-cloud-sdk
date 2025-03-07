package com.emarsys.networking.clients.remoteConfig

import com.emarsys.core.crypto.CryptoApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.remoteConfig.RemoteConfigResponse
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json

class RemoteConfigClient(
    private val networkClient: NetworkClientApi,
    private val urlFactoryApi: UrlFactoryApi,
    private val crypto: CryptoApi,
    private val json: Json,
    private val sdkLogger: Logger
) : RemoteConfigClientApi {
    override suspend fun fetchRemoteConfig(global: Boolean): RemoteConfigResponse? {
        sdkLogger.debug("RemoteConfigClient - fetchRemoteConfig", mapOf("global" to global))

        val toBeConfigBytes = fetchConfig(global)
        val toBeSignatureBytes = fetchSignature(global)
        val config = toBeConfigBytes.await()
        val signature = toBeSignatureBytes.await()
        if (config == null || signature == null) {
            return null
        }
        val verified = crypto.verify(config, signature)
        return if (verified) {
            json.decodeFromString(config)
        } else {
            null
        }
    }

    private suspend fun fetchConfig(global: Boolean): Deferred<String?> = coroutineScope {
        async {
            val request =
                UrlRequest(
                    urlFactoryApi.create(if (global) EmarsysUrlType.GLOBAL_REMOTE_CONFIG else EmarsysUrlType.REMOTE_CONFIG),
                    HttpMethod.Get
                )
            executeRequest(request)
        }
    }

    private suspend fun fetchSignature(global: Boolean): Deferred<String?> = coroutineScope {
        async {
            val request = UrlRequest(
                urlFactoryApi.create(if (global) EmarsysUrlType.GLOBAL_REMOTE_CONFIG_SIGNATURE else EmarsysUrlType.REMOTE_CONFIG_SIGNATURE),
                HttpMethod.Get
            )
            executeRequest(request)
        }
    }

    private suspend fun executeRequest(request: UrlRequest): String? {
        return try {
            networkClient.send(request).let {
                if (it.status.isSuccess()) {
                    it.bodyAsText
                } else null
            }
        } catch (e: Exception) {
            sdkLogger.error("RemoteConfigClient - executeRequest", e)
            null
        }
    }

}