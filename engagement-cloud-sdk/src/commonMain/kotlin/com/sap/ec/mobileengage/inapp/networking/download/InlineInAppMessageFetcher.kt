package com.sap.ec.mobileengage.inapp.networking.download

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.mobileengage.inapp.InAppMessage
import com.sap.ec.mobileengage.inapp.networking.models.InlineMessageRequest
import com.sap.ec.mobileengage.inapp.networking.models.InlineMessageResponse
import com.sap.ec.mobileengage.inapp.presentation.InAppType
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
        return try {
            val url = urlFactory.create(ECUrlType.FetchInlineInAppMessages)
            val requestBody = json.encodeToString(InlineMessageRequest(viewIds = listOf(viewId)))
            val request = UrlRequest(url, HttpMethod.Post, requestBody)

            val response = networkClient.send(request).getOrElse {
                sdkLogger.error("Failed to fetch inline messages for viewId: $viewId", it)
                null
            }

            response?.let {
                createInAppFromResponse(response, viewId)
            }
        } catch (e: Exception) {
            sdkLogger.error(
                "Exception occurred while fetching or decoding inline message for viewId: $viewId",
                e
            )
            null
        }
    }

    private suspend fun createInAppFromResponse(response: Response, viewId: String): InAppMessage? {
        return if (response.status.value == 204) {
            sdkLogger.debug("Received 204 No Content response for viewId: $viewId")
            null
        } else {
            response.bodyAsText.let { body ->
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
        }
    }

    override suspend fun fetch(url: Url, trackingInfo: String): InAppMessage? {
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
                        trackingInfo = trackingInfo,
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
