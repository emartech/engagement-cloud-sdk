package com.emarsys.mobileengage.embeddedmessaging.pagination

import com.emarsys.core.channel.SdkEventManagerApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.body
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.embedded.messaging.model.MessagesResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)

internal class EmbeddedMessagingPaginationHandler(
    private val sdkEventManager: SdkEventManagerApi,
    private val applicationScope: CoroutineScope,
    private val sdkLogger: Logger,
    private val paginationState: EmbeddedMessagingPaginationState
) : EmbeddedMessagingPaginationHandlerApi {
    override suspend fun register() {
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            sdkLogger.debug("register EmbeddedMessagingPagination")
            consumeEvents()
        }
    }

    override fun isEndReached() = paginationState.endReached

    private suspend fun consumeEvents() {
        sdkEventManager.sdkEventFlow
            .filter {
                it is SdkEvent.Internal.EmbeddedMessaging.FetchMessages
                        || it is SdkEvent.Internal.Sdk.Answer.Response<*>
                        || it is SdkEvent.Internal.EmbeddedMessaging.NextPage
            }
            .collect { event ->
                try {
                    when (event) {
                        is SdkEvent.Internal.EmbeddedMessaging.FetchMessages -> {
                            paginationState.refresh()
                            paginationState.lastFetchMessagesId = event.id
                            paginationState.offset = event.offset
                            paginationState.categoryIds = event.categoryIds
                            paginationState.filterUnreadMessages = event.filterUnreadMessages
                        }

                        is SdkEvent.Internal.Sdk.Answer.Response<*> -> {
                            if (event.originId == paginationState.lastFetchMessagesId) {
                                event.result.fold(
                                    onSuccess = { result ->
                                        if (result !is Response) {
                                            sdkLogger.debug("Response produced by this handler should be ignored.")
                                            return@fold
                                        }
                                        val response: MessagesResponse = result.body()
                                        paginationState.top = response.top
                                        val received = response.messages.size
                                        paginationState.receivedCount += received
                                        if (received == 0 || (paginationState.top > 0 && received < paginationState.top)) {
                                            paginationState.endReached = true
                                            sdkLogger.debug("Can't fetch more messages, final page reached")
                                        }
                                        sdkEventManager.emitEvent(
                                            SdkEvent.Internal.Sdk.Answer.Response(
                                                originId = event.originId,
                                                result = Result.success(response)
                                            )
                                        )
                                    },
                                    onFailure = { throwable ->
                                        sdkLogger.error(
                                            "Failed to process embedded messages response",
                                            throwable
                                        )
                                    }
                                )
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
                                        categoryIds = paginationState.categoryIds,
                                        filterUnreadMessages = paginationState.filterUnreadMessages
                                    )
                                )
                            } else {
                                sdkLogger.debug("Can't fetch more messages, final page reached")
                            }
                        }

                        else -> {
                            sdkLogger.debug("Ignoring unrelated event: ${event::class.simpleName}")
                        }
                    }
                } catch (e: Exception) {
                    coroutineContext.ensureActive()
                    sdkLogger.error("Error processing pagination event: $event", e)
                }
            }
    }
}