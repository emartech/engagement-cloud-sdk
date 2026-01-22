package com.emarsys.mobileengage.embeddedmessaging.networking

import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.event.SdkEvent
import io.ktor.http.HttpMethod
import io.ktor.http.buildUrl
import io.ktor.http.takeFrom
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)

internal class EmbeddedMessagesRequestFactory(
    private val urlFactory: UrlFactoryApi,
    private val json: Json
) : EmbeddedMessagingRequestFactoryApi {

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
                        filterUnopenedMessages = embeddedMessagingEvent.filterUnopenedMessages
                    )
                )

            is SdkEvent.Internal.EmbeddedMessaging.FetchMeta ->
                UrlRequest(
                    url = urlFactory.create(EmarsysUrlType.FetchMeta),
                    method = HttpMethod.Get
                )


            is SdkEvent.Internal.EmbeddedMessaging.FetchBadgeCount ->
                UrlRequest(
                    url = urlFactory.create(EmarsysUrlType.FetchBadgeCount),
                    method = HttpMethod.Get
                )

            is SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages ->
                UrlRequest(
                    urlFactory.create(EmarsysUrlType.UpdateTagsForMessages),
                    HttpMethod.Patch,
                    bodyString = json.encodeToString(embeddedMessagingEvent.updateData)
                )
        }
    }

    private fun createFetchMessagesRequest(embeddedMessagingEvent: SdkEvent.Internal.EmbeddedMessaging.FetchMessages): UrlRequest {
        val url = buildUrl {
            takeFrom(urlFactory.create(EmarsysUrlType.FetchEmbeddedMessages))
            if (embeddedMessagingEvent.offset > 0) {
                parameters.append("\$skip", embeddedMessagingEvent.offset.toString())
            }
            if (embeddedMessagingEvent.categoryIds.isNotEmpty()) {
                parameters.append(
                    "filterCategoryIds",
                    embeddedMessagingEvent.categoryIds.joinToString(",")
                )
            }
            if (embeddedMessagingEvent.filterUnopenedMessages) {
                parameters.append(
                    "filterUnopened",
                    "true"
                )
            }
        }
        return UrlRequest(
            url = url,
            method = HttpMethod.Get,
        )
    }
}