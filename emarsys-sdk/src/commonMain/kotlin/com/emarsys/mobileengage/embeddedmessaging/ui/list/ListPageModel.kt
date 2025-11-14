package com.emarsys.mobileengage.embeddedmessaging.ui.list

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.body
import com.emarsys.event.SdkEvent
import com.emarsys.networking.clients.embedded.messaging.model.BadgeCountResponse
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.emarsys.networking.clients.embedded.messaging.model.MessagesResponse

internal class ListPageModel(
    private val sdkEventDistributor: SdkEventDistributorApi
) : ListPageModelApi {
    override suspend fun fetchMessages(): List<EmbeddedMessage> {
        return try {
            val fetchMessagesEvent = SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                nackCount = 0,
                offset = 0,
                categoryIds = emptyList()
            )
            val messagesResponse = sdkEventDistributor.registerEvent(fetchMessagesEvent)
            val response = messagesResponse.await<Response>()

            response.result.fold(
                onSuccess = { networkResponse ->
                    val messagesResponse: MessagesResponse = networkResponse.body()
                    messagesResponse.messages
                },
                onFailure = {
                    emptyList()
                }
            )
        } catch (e: Exception) {
            emptyList()
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
                    0
                }
            )
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun fetchNextPage(offset: Int, categoryIds: List<Int>): List<EmbeddedMessage> {
        return try {
            val fetchNextPageEvent = SdkEvent.Internal.EmbeddedMessaging.FetchNextPage(
                nackCount = 0,
                offset = offset,
                categoryIds = categoryIds
            )
            val nextPageResponse = sdkEventDistributor.registerEvent(fetchNextPageEvent)
            val response = nextPageResponse.await<Response>()

            response.result.fold(
                onSuccess = { networkResponse ->
                    val messagesResponse: MessagesResponse = networkResponse.body()
                    messagesResponse.messages
                },
                onFailure = {
                    emptyList()
                }
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}