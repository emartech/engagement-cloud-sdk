package com.emarsys.mobileengage.embeddedmessaging.pagination

import com.emarsys.core.Registerable
import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.body
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.embedded.messaging.model.MessagesResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

internal class EmbeddedMessagingPaginationHandler(
    private val sdkEventManager: SdkEventManagerApi,
    private val applicationScope: CoroutineScope,
    private val sdkLogger: Logger,
    private val paginationState: EmbeddedMessagingPaginationState
) : Registerable {
    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("register EmbeddedMessagingPagination")
            consumeEvents()
        }
    }

    private suspend fun consumeEvents() {
        sdkEventManager.sdkEventFlow
            .filter {
                it is SdkEvent.Internal.EmbeddedMessaging.FetchMessages
                        || it is SdkEvent.Internal.Sdk.Answer.Response<*>
                        || it is SdkEvent.Internal.EmbeddedMessaging.NextPage
                        || it is SdkEvent.Internal.EmbeddedMessaging.ResetPagination
            }
            .collect { event ->
                when (event) {
                    is SdkEvent.Internal.EmbeddedMessaging.FetchMessages -> {
                        paginationState.lastFetchMessagesId = event.id
                        paginationState.offset = event.offset
                        paginationState.categoryIds = event.categoryIds
                    }
                    is SdkEvent.Internal.Sdk.Answer.Response<*> -> {
                        if (event.originId == paginationState.lastFetchMessagesId) {
                            event.result.onSuccess { result ->
                                val response: MessagesResponse = (result as Response).body()
                                paginationState.top = response.top
                                val received = response.messages.size
                                paginationState.receivedCount += received
                                if (received == 0 || (paginationState.top > 0 && received < paginationState.top)) {
                                    paginationState.endReached = true
                                    sdkLogger.debug("Can't fetch more messages, final page reached")
                                }
                            }
                            event.result.onFailure { throwable ->
                                sdkLogger.error("Failed to process embedded messages response", throwable)
                            }
                        } else {
                            sdkLogger.debug("Ignoring response with mismatched originId=${event.originId}")
                        }
                    }
                    is SdkEvent.Internal.EmbeddedMessaging.NextPage -> {
                        if (paginationState.canFetchNextPage()) {
                            paginationState.updateOffset()
                            paginationState.lastFetchMessagesId = event.id
                            sdkEventManager.registerEvent(
                                SdkEvent.Internal.EmbeddedMessaging.FetchNextPage(
                                    id = event.id,
                                    timestamp = event.timestamp,
                                    nackCount = 0,
                                    offset = paginationState.offset,
                                    categoryIds = paginationState.categoryIds
                                )
                            )
                        } else {
                            sdkLogger.debug("Can't fetch more messages, final page reached")
                        }
                    }
                    is SdkEvent.Internal.EmbeddedMessaging.ResetPagination -> {
                        sdkLogger.debug("Resetting pagination state")
                        paginationState.reset()
                    }
                    else -> {
                        sdkLogger.debug("Ignoring unrelated event: ${event::class.simpleName}")
                    }
                }
            }
    }
}