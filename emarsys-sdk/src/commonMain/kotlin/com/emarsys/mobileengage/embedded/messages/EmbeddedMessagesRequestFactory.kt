package com.emarsys.mobileengage.embedded.messages

import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.SdkEvent
import io.ktor.http.HttpMethod
import io.ktor.http.buildUrl

import io.ktor.http.takeFrom

internal class EmbeddedMessagesRequestFactory(
    private val urlFactory: UrlFactoryApi
) {

    fun create(embeddedMessagingEvent: SdkEvent.Internal.EmbeddedMessaging): UrlRequest {
        return when (embeddedMessagingEvent) {
            is SdkEvent.Internal.EmbeddedMessaging.FetchMessages ->
                createFetchMessagesRequest(embeddedMessagingEvent)

            is SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount ->
                UrlRequest(
                    url = urlFactory.create(EmarsysUrlType.FETCH_BADGE_COUNT),
                    method = HttpMethod.Get
                )

            else -> createFetchMessagesRequest(embeddedMessagingEvent as SdkEvent.Internal.EmbeddedMessaging.FetchMessages)
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