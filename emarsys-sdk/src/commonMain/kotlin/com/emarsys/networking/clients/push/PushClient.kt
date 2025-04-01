package com.emarsys.networking.clients.push

import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType.PUSH_TOKEN
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.push.model.PushToken
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json

internal class PushClient(
    private val emarsysClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val json: Json
) : PushClientApi {
    override suspend fun registerPushToken(pushToken: String) {
        val url = urlFactory.create(PUSH_TOKEN, null)
        val body = json.encodeToString(PushToken(pushToken))
        emarsysClient.send(UrlRequest(url, HttpMethod.Put, body))
    }

    override suspend fun clearPushToken() {
        val url = urlFactory.create(PUSH_TOKEN, null)
        emarsysClient.send(UrlRequest(url, HttpMethod.Delete))
    }
}