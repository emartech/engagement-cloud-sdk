package com.emarsys.networking.clients.deepLink

import com.emarsys.core.log.Logger
import com.emarsys.core.networking.UserAgentProvider
import com.emarsys.core.networking.UserAgentProviderApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.di.SdkComponent
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class DeepLinkClient(
    private val networkClient: NetworkClientApi,
    private val sdkEventFlow: MutableSharedFlow<SdkEvent>,
    private val urlFactory: UrlFactoryApi,
    private val userAgentProvider: UserAgentProviderApi,
    private val json: Json,
    private val sdkLogger: Logger,
    sdkDispatcher: CoroutineDispatcher,
) : SdkComponent {

    init {
        CoroutineScope(sdkDispatcher).launch {
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventFlow
            .filter { it is SdkEvent.Internal.Sdk.TrackDeepLink }
            .collect {
                val trackingId = it.attributes?.get("trackingId")?.jsonPrimitive?.content
                val requestBody = buildJsonObject { put("ems_dl", JsonPrimitive(trackingId)) }
                val headers =
                    mapOf(UserAgentProvider.USER_AGENT_HEADER_NAME to userAgentProvider.provide())
                val request = UrlRequest(
                    urlFactory.create(EmarsysUrlType.DEEP_LINK, null),
                    method = HttpMethod.Post,
                    headers = headers,
                    bodyString = json.encodeToString(requestBody)
                )
                try {
                    networkClient.send(request)
                } catch (e: Exception) {
                    sdkLogger.error("DeepLinkClient - trackDeepLink(trackId: \"$trackingId\")", e)
                }
            }
    }
}