package com.emarsys.networking.clients.embedded.messaging

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embedded.messages.EmbeddedMessagingRequestFactoryApi
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class EmbeddedMessagingClient(
    private val sdkLogger: Logger,
    private val sdkEventManager: SdkEventManagerApi,
    private val applicationScope: CoroutineScope,
    private val embeddedMessagingRequestFactory: EmbeddedMessagingRequestFactoryApi,
    private val emarsysNetworkClient: NetworkClientApi,
    private val eventsDao: EventsDaoApi,
    private val clientExceptionHandler: ClientExceptionHandler
) : EventBasedClientApi {
    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("register EmbeddedMessagingClient")
            startEventConsumer()
        }
    }

    private suspend fun startEventConsumer() {
        sdkEventManager.onlineSdkEvents
            .filter {
                it is SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount
                        || it is SdkEvent.Internal.EmbeddedMessaging.FetchMessages
                        || it is SdkEvent.Internal.EmbeddedMessaging.FetchMeta
                        || it is SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages
            }
            .collect {
                try {
                    sdkLogger.debug("consume EmbeddedMessaging events")
                    val request =
                        embeddedMessagingRequestFactory.create(it as SdkEvent.Internal.EmbeddedMessaging)
                    val response = emarsysNetworkClient.send(request) {
                        sdkEventManager.emitEvent(it)
                    }
                    it.ack(eventsDao, sdkLogger)
                    sdkEventManager.emitEvent(SdkEvent.Internal.Sdk.Answer.Response(it.id, Result.success(response)))
                } catch (exception: Exception) {
                    sdkEventManager.emitEvent(
                        SdkEvent.Internal.Sdk.Answer.Response(
                            it.id,
                            Result.failure<Exception>(exception)
                        )
                    )
                    clientExceptionHandler.handleException(
                        exception,
                        "exception while consuming EmbeddedMessaging events",
                        it
                    )
                }
            }
    }
}