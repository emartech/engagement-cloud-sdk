package com.emarsys.mobileengage.embedded.messages

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
                        || (it is SdkEvent.Internal.Sdk.Answer.Response<*> && it.originId == paginationState.lastFetchMessagesId)
                        || it is SdkEvent.Internal.EmbeddedMessaging.NextPage
            }
            .collect {
                if (it is SdkEvent.Internal.EmbeddedMessaging.FetchMessages) {
                    paginationState.lastFetchMessagesId = it.id
                    paginationState.offset = it.offset
                    paginationState.categoryIds = it.categoryIds
                } else if (it is SdkEvent.Internal.Sdk.Answer.Response<*>) {
                    it.result.onSuccess { result ->
                        val response: MessagesResponse = (result as Response).body()
                        paginationState.top = response.top
                        paginationState.count = response.count
                    }
                    it.result.onFailure { throwable -> } //TODO
                } else if (it is SdkEvent.Internal.EmbeddedMessaging.NextPage) {
                        if (paginationState.canFetchNextPage()) {
                            paginationState.updateOffset()
                            sdkEventManager.registerEvent(
                                SdkEvent.Internal.EmbeddedMessaging.FetchNextPage(
                                    id = it.id,
                                    timestamp = it.timestamp,
                                    nackCount = 0,
                                    offset = paginationState.offset,
                                    categoryIds = paginationState.categoryIds
                                )
                            )
                        } else {
                            sdkLogger.debug("Can't fetch more messages, final page reached")
                        }
                }
            }
    }
}