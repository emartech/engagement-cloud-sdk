package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.body
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.embeddedmessaging.pagination.EmbeddedMessagingPaginationHandlerApi
import com.emarsys.networking.clients.embedded.messaging.model.BadgeCountResponse
import com.emarsys.networking.clients.embedded.messaging.model.MessagesResponse
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ListPageModel(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val embeddedMessagingPaginationHandler: EmbeddedMessagingPaginationHandlerApi,
    private val sdkLogger: Logger
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
            val fetchMessagesResponse =
                sdkEventDistributor.registerEvent(fetchMessagesEvent).await(MessagesResponse::class)

            fetchMessagesResponse.result.fold(
                onSuccess = { messagesResponse ->
                    Result.success(
                        MessagesWithCategories(
                            messagesResponse.meta.categories,
                            messagesResponse.messages,
                            isEndReached = embeddedMessagingPaginationHandler.isEndReached()
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
            val badgeCountResponse = sdkEventDistributor.registerEvent(fetchBadgeCountEvent)
            val response = badgeCountResponse.await<Response>()

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
            val nextPageResponse =
                sdkEventDistributor.registerEvent(SdkEvent.Internal.EmbeddedMessaging.NextPage())
                    .await(MessagesResponse::class)

            nextPageResponse.result.fold(
                onSuccess = { messagesResponse ->
                    Result.success(
                        MessagesWithCategories(
                            messagesResponse.meta.categories,
                            messagesResponse.messages,
                            isEndReached = embeddedMessagingPaginationHandler.isEndReached()
                        )
                    )
                },
                onFailure = {
                    sdkLogger.error("FetchNextPage failure.", it)
                    Result.failure(it)
                }
            )
        } catch (e: Exception) {
            sdkLogger.error("FetchNextPage exception.", e)
            Result.failure(e)
        }
    }
}