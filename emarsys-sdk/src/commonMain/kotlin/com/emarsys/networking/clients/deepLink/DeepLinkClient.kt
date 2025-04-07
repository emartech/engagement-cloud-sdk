package com.emarsys.networking.clients.deepLink

import com.emarsys.core.Registerable
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.FailedRequestException
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.exceptions.RetryLimitReachedException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.UserAgentProvider
import com.emarsys.core.networking.UserAgentProviderApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal class DeepLinkClient(
    private val networkClient: NetworkClientApi,
    private val sdkEventManager: SdkEventManagerApi,
    private val urlFactory: UrlFactoryApi,
    private val userAgentProvider: UserAgentProviderApi,
    private val eventsDao: EventsDaoApi,
    private val json: Json,
    private val sdkLogger: Logger,
    private val applicationScope: CoroutineScope,
) : Registerable {

    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.onlineSdkEvents
            .filter { it is SdkEvent.Internal.Sdk.TrackDeepLink }
            .collect {
                try {
                    val trackingId = it.attributes?.get("trackingId")?.jsonPrimitive?.content
                    val requestBody = buildJsonObject { put("ems_dl", trackingId) }
                    val headers =
                        mapOf(UserAgentProvider.USER_AGENT_HEADER_NAME to userAgentProvider.provide())
                    val request = UrlRequest(
                        urlFactory.create(EmarsysUrlType.DEEP_LINK, null),
                        method = HttpMethod.Post,
                        headers = headers,
                        bodyString = json.encodeToString(requestBody)
                    )
                    networkClient.send(
                        request,
                        onNetworkError = { sdkEventManager.emitEvent(it) })
                    it.ack(eventsDao, sdkLogger)
                } catch (exception: Exception) {
                    when (exception) {
                        is FailedRequestException, is RetryLimitReachedException, is MissingApplicationCodeException -> it.ack(
                            eventsDao,
                            sdkLogger
                        )

                        else -> sdkLogger.error(
                            "DeepLinkClient - trackDeepLink(trackId: \"${it.attributes?.get("trackingId")?.jsonPrimitive?.content}\")",
                            exception
                        )
                    }
                }
            }
    }
}