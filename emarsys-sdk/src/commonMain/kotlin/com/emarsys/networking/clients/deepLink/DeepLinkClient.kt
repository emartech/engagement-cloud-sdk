package com.emarsys.networking.clients.deepLink

import com.emarsys.core.log.Logger
import com.emarsys.core.networking.UserAgentProvider
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.providers.SuspendProvider
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class DeepLinkClient(private val networkClient: NetworkClientApi,
                     private val urlFactory: UrlFactoryApi,
                     private val userAgentProvider: SuspendProvider<String>,
                     private val json: Json,
                     private val sdkLogger: Logger): DeepLinkClientApi {

    override suspend fun trackDeepLink(trackingId: String) {
        val requestBody = buildJsonObject { put("ems_dl", JsonPrimitive(trackingId)) }
        val headers = mapOf(UserAgentProvider.USER_AGENT_HEADER_NAME to userAgentProvider.provide())
        val request = UrlRequest(urlFactory.create(EmarsysUrlType.DEEP_LINK), method = HttpMethod.Post, headers = headers,
            bodyString = json.encodeToString(requestBody)
        )
        try {
            networkClient.send(request)
        } catch (e: Exception) {
            sdkLogger.error("DeepLinkClient - trackDeepLink(trackId: \"$trackingId\")", e)
        }
    }
}