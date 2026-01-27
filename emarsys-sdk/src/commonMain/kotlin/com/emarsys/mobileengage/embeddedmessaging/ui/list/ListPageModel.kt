package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.body
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embeddedmessaging.exceptions.LastPageReachedException
import com.emarsys.mobileengage.embeddedmessaging.pagination.EmbeddedMessagingPaginationState
import com.emarsys.networking.clients.embedded.messaging.model.BadgeCountResponse
import com.emarsys.networking.clients.embedded.messaging.model.MessagesResponse
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ListPageModel(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger,
    private var paginationState: EmbeddedMessagingPaginationState
) : ListPageModelApi {

    override suspend fun fetchMessagesWithCategories(
        filterUnopenedOnly: Boolean,
        categoryIds: List<Int>
    ): Result<MessagesWithCategories> {
        return try {
            val fetchMessagesEvent = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                nackCount = 0,
                offset = 0,
                categoryIds = categoryIds,
                filterUnopenedMessages = filterUnopenedOnly
            )

            paginationState = EmbeddedMessagingPaginationState(
                totalReceivedCount = 0,
                isEndReached = false,
                categoryIds = categoryIds,
                filterUnopenedMessages = filterUnopenedOnly
            )

            val fetchMessagesResponse =
                sdkEventDistributor.registerEvent(fetchMessagesEvent).await<Response>()

            fetchMessagesResponse.result.fold(
                onSuccess = { messagesSuccessResponse ->
                    val messagesResponse: MessagesResponse = messagesSuccessResponse.body()
                    updatePaginationState(messagesResponse)
                    Result.success(
                        MessagesWithCategories(
                            messagesResponse.meta.categories,
                            messagesResponse.messages,
                            isEndReached = paginationState.isEndReached
                        )
                    )
                },
                onFailure = {
                    sdkLogger.error("FetchMessagesWithCategories failure.", it)
                    Result.failure(it)
                }
            )
        } catch (e: Exception) {
            sdkLogger.error("FetchMessagesWithCategories exception.", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchBadgeCount(): Int {
        return try {
            val fetchBadgeCountEvent = SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount(
                nackCount = 0
            )
            val response = sdkEventDistributor.registerEvent(fetchBadgeCountEvent).await<Response>()

            response.result.fold(
                onSuccess = { networkResponse ->
                    val badgeCountResponse: BadgeCountResponse = networkResponse.body()
                    badgeCountResponse.count
                },
                onFailure = {
                    sdkLogger.error("FetchBadgeCount failure.", it)
                    0
                }
            )
        } catch (e: Exception) {
            sdkLogger.error("FetchBadgeCount exception.", e)
            0
        }
    }

    override suspend fun fetchNextPage(): Result<MessagesWithCategories> {
        return try {
            if (!paginationState.isEndReached) {
                val nextPageResponse = sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.EmbeddedMessaging.FetchNextPage(
                        nackCount = 0,
                        offset = paginationState.totalReceivedCount,
                        categoryIds = paginationState.categoryIds,
                        filterUnopenedMessages = paginationState.filterUnopenedMessages
                    )
                ).await<Response>()
                nextPageResponse.result.fold(
                    onSuccess = { messagesResponse ->
                        val messagesResponse: MessagesResponse = messagesResponse.body()
                        updatePaginationState(messagesResponse)
                        Result.success(
                            MessagesWithCategories(
                                messagesResponse.meta.categories,
                                messagesResponse.messages,
                                isEndReached = paginationState.isEndReached
                            )
                        )
                    },
                    onFailure = {
                        sdkLogger.error("FetchNextPage failure.", it)
                        Result.failure(it)
                    }
                )
            } else {
                sdkLogger.debug("Can't fetch more messages, final page reached")
                Result.failure(LastPageReachedException("Can't fetch more pages because last page reached"))
            }
        } catch (e: Exception) {
            sdkLogger.error("FetchNextPage exception.", e)
            Result.failure(e)
        }
    }

    private fun updatePaginationState(messagesResponse: MessagesResponse) {
        val fetchedMessagesCount = messagesResponse.messages.size
        val messagesPerPage = messagesResponse.top

        paginationState = paginationState.copy(
            totalReceivedCount = paginationState.totalReceivedCount + fetchedMessagesCount,
            isEndReached = fetchedMessagesCount == 0 || fetchedMessagesCount < messagesPerPage
        )
    }
}