package com.emarsys.core.networking.clients.push

import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.clients.push.model.PushToken
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.EmarsysUrlType.REGISTER_PUSH_TOKEN
import com.emarsys.url.FactoryApi
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PushClient(
    private val emarsysClient: NetworkClientApi,
    private val urlFactory: FactoryApi<EmarsysUrlType, String>,
    private val json: Json
) : PushClientApi {
    override suspend fun registerPushToken(pushToken: String) {
        val url = Url(urlFactory.create(REGISTER_PUSH_TOKEN))
        val body = json.encodeToString(PushToken(pushToken))
        emarsysClient.send(UrlRequest(url, HttpMethod.Put, body))
    }

    override suspend fun clearPushToken() {
    }
}