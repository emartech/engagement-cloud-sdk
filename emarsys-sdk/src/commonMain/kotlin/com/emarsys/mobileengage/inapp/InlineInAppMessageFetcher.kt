package com.emarsys.mobileengage.inapp

import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class InlineInAppMessageFetcher(
    private val networkClient: NetworkClientApi,
    private val urlFactory: UrlFactoryApi,
    private val json: Json
    //TODO: logger
): InlineInAppMessageFetcherApi {

    override suspend fun fetch(viewId: String): InAppMessage? {
        val url = urlFactory.create(EmarsysUrlType.FetchInlineInAppMessages)
        val requestBody = json.encodeToString(InlineMessageRequest(viewIds = listOf(viewId)))
        val request = UrlRequest(url, HttpMethod.Post, requestBody)

        try {
            val response = networkClient.send(request).getOrNull()
            return response?.bodyAsText?.let { body ->
                val responseModel = json.decodeFromString<InlineMessageResponse>(body)
                responseModel.inlineMessages?.find {
                    it.viewId.equals(viewId, ignoreCase = true)
                }?.let { inAppMessage ->
                    return if (inAppMessage.content.isEmpty()) {
                        null
                    } else {
                        InAppMessage(
                            type = parseInAppType(inAppMessage.type),
                            trackingInfo = inAppMessage.trackingInfo,
                            content = inAppMessage.content
                        )
                    }
                }
            }
        } catch (e: Exception) {
            return null
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


//TODO: move to separate files
@Serializable
private data class InlineMessageRequest(
    val viewIds: List<String>
)

@Serializable
private data class InlineMessageResponse(
    val inlineMessages: List<InlineMessageItem>? = null
)

@Serializable
private data class InlineMessageItem(
    val type: String,
    val trackingInfo: String,
    val content: String,
    val viewId: String
)
