package com.sap.ec.networking

import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.VISITOR_ID_COOKIE_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.XP_COOKIE_KEY

internal class RecommendationNetworkClient(
    private val genericNetworkClient: NetworkClientApi,
    private val stringStorage: StringStorageApi
) : NetworkClientApi {
    override suspend fun send(request: UrlRequest): Result<Response> {
        val visitorIdCookie = stringStorage.get(VISITOR_ID_COOKIE_KEY)
        val xpCookie = stringStorage.get(XP_COOKIE_KEY)

        val cookieValue = buildString {
            if (!xpCookie.isNullOrEmpty()) append("xp=$xpCookie;")
            if (!visitorIdCookie.isNullOrEmpty()) append("cdv=$visitorIdCookie")
        }

        val populatedRequest = if (cookieValue.isNotEmpty()) {
            request.copy(headers = mapOf("Cookie" to cookieValue))
        } else request

        return genericNetworkClient.send(populatedRequest)
    }
}