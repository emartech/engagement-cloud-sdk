package com.emarsys.mobileengage.embeddedmessaging.networking

import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.SdkEvent
import io.ktor.http.*
import kotlinx.serialization.json.Json

internal class EmbeddedMessagesRequestFactory(
    private val urlFactory: UrlFactoryApi,
    private val json: Json
): EmbeddedMessagingRequestFactoryApi {

   override fun create(embeddedMessagingEvent: SdkEvent.Internal.EmbeddedMessaging): UrlRequest {
        return when (embeddedMessagingEvent) {
            is SdkEvent.Internal.EmbeddedMessaging.FetchMessages ->
                createFetchMessagesRequest(embeddedMessagingEvent)

            is SdkEvent.Internal.EmbeddedMessaging.FetchNextPage ->
                createFetchMessagesRequest(
                    SdkEvent.Internal.EmbeddedMessaging.FetchMessages(
                        id = embeddedMessagingEvent.id,
                        type = embeddedMessagingEvent.type,
                        timestamp = embeddedMessagingEvent.timestamp,
                        nackCount = embeddedMessagingEvent.nackCount,
                        offset = embeddedMessagingEvent.offset,
                        categoryIds = embeddedMessagingEvent.categoryIds,
                    )
                )

            is SdkEvent.Internal.EmbeddedMessaging.FetchMeta ->
                UrlRequest(
                    url = urlFactory.create(EmarsysUrlType.FETCH_META),
                    method = HttpMethod.Get
                )


            is SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount ->
                UrlRequest(
                    url = urlFactory.create(EmarsysUrlType.FETCH_BADGE_COUNT),
                    method = HttpMethod.Get
                )

            is SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages ->
                UrlRequest(
                    urlFactory.create(EmarsysUrlType.UPDATE_TAGS_FOR_MESSAGES),
                    HttpMethod.Patch,
                    bodyString = json.encodeToString(embeddedMessagingEvent.updateData)
                )
        }
    }

    private fun createFetchMessagesRequest(embeddedMessagingEvent: SdkEvent.Internal.EmbeddedMessaging.FetchMessages): UrlRequest {
        val url = buildUrl {
            takeFrom(urlFactory.create(EmarsysUrlType.FETCH_EMBEDDED_MESSAGES))
            if (embeddedMessagingEvent.offset > 0) {
                parameters.append("skip", embeddedMessagingEvent.offset.toString())
            }
            if (embeddedMessagingEvent.categoryIds.isNotEmpty()) {
                parameters.append(
                    "categoryIds",
                    embeddedMessagingEvent.categoryIds.joinToString(",")
                )
            }
        }
        return UrlRequest(
            url = url,
            method = HttpMethod.Get,
        )
    }
}