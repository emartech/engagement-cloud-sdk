package com.emarsys.clients.push

import com.emarsys.clients.push.model.PushToken
import com.emarsys.context.SdkContextApi
import com.emarsys.core.DefaultUrlsApi
import com.emarsys.core.networking.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PushClient(
    private val networkClient: NetworkClientApi,
    private val sdkContext: SdkContextApi,
    private val defaultUrls: DefaultUrlsApi,
    private val json: Json
) : PushClientApi {
    override suspend fun registerPushToken(pushToken: String) {
        val url = sdkContext.createUrl(
            baseUrl = defaultUrls.clientServiceBaseUrl,
            version = "v3",
            withAppCode = true,
            path = "/client/push-token"
        )
        val body = json.encodeToString(PushToken(pushToken))
        networkClient.send(UrlRequest(url, HttpMethod.Put, body))
    }
}