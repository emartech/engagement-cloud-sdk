package com.emarsys.networking.clients.embedded.messaging

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.db.events.EventsDaoApi
import com.emarsys.core.exceptions.SdkException.NetworkIOException
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embeddedmessaging.networking.EmbeddedMessagingRequestFactoryApi
import com.emarsys.networking.clients.EventBasedClientApi
import com.emarsys.networking.clients.error.ClientExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
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

    @OptIn(ExperimentalTime::class)
    private suspend fun startEventConsumer() {
        sdkEventManager.onlineSdkEvents
            .filter {
                it is SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount
                        || it is SdkEvent.Internal.EmbeddedMessaging.FetchMessages
                        || it is SdkEvent.Internal.EmbeddedMessaging.FetchMeta
                        || it is SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages
                        || it is SdkEvent.Internal.EmbeddedMessaging.FetchNextPage
            }
            .collect {
                try {
                    sdkLogger.debug("consume EmbeddedMessaging events")
                    val request =
                        embeddedMessagingRequestFactory.create(it as SdkEvent.Internal.EmbeddedMessaging)
                    when (it) {
                        is SdkEvent.Internal.EmbeddedMessaging.FetchMessages -> {
                            emarsysNetworkClient.send(request)
                                .onSuccess { response ->
                                    handleSuccess(it, response)
                                }.onFailure { exception ->
                                    handleException(exception, it)
                                }
                        }

                        else -> {
                            emarsysNetworkClient.send(request)
                                .onSuccess { response ->
                                    handleSuccess(it, response)
                                }.onFailure { exception ->
                                    handleException(exception, it)
                                }
                        }
                    }
                } catch (e: Exception) {
                    handleException(e, it)
                }
            }
    }

    private suspend fun handleSuccess(
        event: OnlineSdkEvent,
        response: Response
    ) {
        sdkEventManager.emitEvent(
            SdkEvent.Internal.Sdk.Answer.Response(
                event.id,
                Result.success(response)
            )
        )
        event.ack(eventsDao, sdkLogger)
    }

    private suspend fun handleException(
        exception: Throwable,
        messaging: OnlineSdkEvent
    ) {
        if (exception is NetworkIOException) {
            sdkEventManager.emitEvent(messaging)
        } else {
            sdkEventManager.emitEvent(
                SdkEvent.Internal.Sdk.Answer.Response(
                    messaging.id,
                    Result.failure<Exception>(exception)
                )
            )
            clientExceptionHandler.handleException(
                exception,
                "exception while consuming EmbeddedMessaging events",
                messaging
            )
        }
    }
}
