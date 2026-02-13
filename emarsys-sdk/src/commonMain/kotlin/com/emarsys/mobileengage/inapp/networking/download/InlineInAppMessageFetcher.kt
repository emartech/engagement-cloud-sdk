package com.emarsys.mobileengage.inapp.networking.download

import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.networking.models.InlineMessageRequest
import com.emarsys.mobileengage.inapp.networking.models.InlineMessageResponse
import com.emarsys.mobileengage.inapp.presentation.InAppType
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.serialization.json.Json

internal class InlineInAppMessageFetcher(
    private val networkClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val json: Json,
    private val sdkLogger: Logger
) : InlineInAppMessageFetcherApi {

    override suspend fun fetch(viewId: String): InAppMessage? {
        val url = urlFactory.create(EmarsysUrlType.FetchInlineInAppMessages)
        val requestBody = json.encodeToString(InlineMessageRequest(viewIds = listOf(viewId)))
        val request = UrlRequest(url, HttpMethod.Post, requestBody)
        return try {
            networkClient.send(request).getOrElse {
                sdkLogger.error("Failed to fetch inline messages for viewId: $viewId", it)
                return null
            }.bodyAsText.let { body ->
                val responseModel = json.decodeFromString<InlineMessageResponse>(body)
                responseModel.inlineMessages?.find {
                    it.viewId.equals(viewId, ignoreCase = true)
                }?.let { inAppMessage ->
                    if (inAppMessage.content.isEmpty()) {
                        sdkLogger.debug("Inline message content is empty for viewId: $viewId")
                        null
                    } else {
                        sdkLogger.debug("Successfully fetched inline message for viewId: $viewId")
                        InAppMessage(
                            type = parseInAppType(inAppMessage.type),
                            trackingInfo = inAppMessage.trackingInfo,
                            content = inAppMessage.content
                        )
                    }
                }
            }
        } catch (e: Exception) {
            sdkLogger.error(
                "Exception occurred while fetching or decoding inline message for viewId: $viewId",
                e
            )
            return null
        }
    }

    override suspend fun fetch(url: Url): InAppMessage? {
        val request = UrlRequest(url, HttpMethod.Get)
        return try {
            networkClient.send(request).getOrElse {
                sdkLogger.error("Failed to fetch inline message from url: $url", it)
                return null
            }.bodyAsText.let { content ->
                if (content.isEmpty()) {
                    sdkLogger.debug("Inline message content is empty from url: $url")
                    null
                } else {
                    sdkLogger.debug("Successfully fetched inline message from url: $url")
                    InAppMessage(
                        type = InAppType.INLINE,
                        trackingInfo = "inlineInAppTrackingInfo",
                        content = content
                    )
                }
            }
        } catch (e: Exception) {
            sdkLogger.error("Exception occurred while fetching inline message from url: $url", e)
            null
        }
    }

    private fun parseInAppType(type: String): InAppType {
        return when (type.lowercase()) {
            "inline" -> InAppType.INLINE
            "overlay" -> InAppType.OVERLAY
            else -> InAppType.OVERLAY
        }
    }
}
