package com.sap.ec.networking.clients.deepLink

import com.sap.ec.core.channel.SdkEventManagerApi
import com.sap.ec.core.db.events.EventsDaoApi
import com.sap.ec.core.exceptions.SdkException.NetworkIOException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.UserAgentProvider
import com.sap.ec.core.networking.UserAgentProviderApi
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.networking.clients.EventBasedClientApi
import com.sap.ec.networking.clients.error.ClientExceptionHandler
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
                        urlFactory.create(ECUrlType.DeepLink),
                        method = HttpMethod.Post,
                        headers = headers,
                        bodyString = json.encodeToString(requestBody)
                    )

                    val response = networkClient.send(
                        request
                    )
                    response.onSuccess {
                        sdkEvent.ack(eventsDao, sdkLogger)
                        sdkEventManager.emitEvent(
                            SdkEvent.Internal.Sdk.Answer.Response(
                                originId = sdkEvent.id,
                                Result.success(it)
                            )
                        )
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