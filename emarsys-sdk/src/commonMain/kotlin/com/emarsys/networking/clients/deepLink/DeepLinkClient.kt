package com.emarsys.networking.clients.deepLink

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.SdkException.NetworkIOException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.UserAgentProvider
import com.emarsys.core.networking.UserAgentProviderApi
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class DeepLinkClient(
    private val networkClient: NetworkClientApi,
    private val clientExceptionHandler: ClientExceptionHandler,
    private val sdkEventManager: SdkEventManagerApi,
    private val urlFactory: UrlFactoryApi,
    private val userAgentProvider: UserAgentProviderApi,
    private val eventsDao: EventsDaoApi,
    private val json: Json,
    private val sdkLogger: Logger,
    private val applicationScope: CoroutineScope,
) : EventBasedClientApi {

    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("Register")
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.onlineSdkEvents
            .filterIsInstance<SdkEvent.Internal.Sdk.TrackDeepLink>()
            .collect { sdkEvent ->
                try {
                    val trackingId = sdkEvent.trackingId
                    val requestBody = buildJsonObject { put("ems_dl", trackingId) }
                    val headers =
                        mapOf(UserAgentProvider.USER_AGENT_HEADER_NAME to userAgentProvider.provide())
                    val request = UrlRequest(
                        urlFactory.create(EmarsysUrlType.DeepLink),
                        method = HttpMethod.Post,
                        headers = headers,
                        bodyString = json.encodeToString(requestBody)
                    )

                    val response = networkClient.send(
                        request
                    )
                    response.onSuccess {
                        sdkEvent.ack(eventsDao, sdkLogger)
                    }
                    response.onFailure { error ->
                        handleException(error, sdkEvent)
                    }
                } catch (exception: Exception) {
                    handleException(exception, sdkEvent)
                }
            }
    }

    private suspend fun handleException(
        exception: Throwable,
        sdkEvent: SdkEvent.Internal.Sdk.TrackDeepLink
    ) {
        if (exception is NetworkIOException) {
            sdkEventManager.emitEvent(sdkEvent)
        } else {
            SdkEvent.Internal.Sdk.Answer.Response(
                originId = sdkEvent.id,
                Result.failure<Exception>(exception)
            )

        }
        clientExceptionHandler.handleException(
            exception,
            "DeepLinkClient - trackDeepLink(trackId: ${sdkEvent.trackingId})",
            sdkEvent
        )
    }
}