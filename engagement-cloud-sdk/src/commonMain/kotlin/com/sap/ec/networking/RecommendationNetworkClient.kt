package com.sap.ec.networking

import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.UserAgentProvider
import com.sap.ec.core.networking.UserAgentProviderApi
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.VISITOR_ID_COOKIE_KEY
import com.sap.ec.mobileengage.recommendation.RecommendationConstants.XP_COOKIE_KEY
import com.sap.ec.networking.ECHeaders.CONTACT_TOKEN_HEADER

internal class RecommendationNetworkClient(
    private val genericNetworkClient: NetworkClientApi,
    private val stringStorage: StringStorageApi,
    private val requestContext: RequestContextApi,
    private val userAgentProvider: UserAgentProviderApi,
    private val sdkLogger: Logger
) : NetworkClientApi {
    override suspend fun send(request: UrlRequest): Result<Response> {
        val contactToken = requestContext.contactToken
        if (contactToken.isNullOrEmpty()) {
            sdkLogger.error("Contact token is missing")
            return Result.failure(SdkException.ContactTokenNotFoundException("Contact token is missing"))
        }
        val visitorIdCookie = stringStorage.get(VISITOR_ID_COOKIE_KEY)
        val xpCookie = stringStorage.get(XP_COOKIE_KEY)

        val cookieValue = buildString {
            if (!xpCookie.isNullOrEmpty()) append("xp=$xpCookie;")
            if (!visitorIdCookie.isNullOrEmpty()) append("cdv=$visitorIdCookie")
        }

        val headers = buildMap {
            if (cookieValue.isNotEmpty()) put("Cookie", cookieValue)
            if (contactToken.isNotEmpty()) put(
                CONTACT_TOKEN_HEADER,
                contactToken
            )
            put(UserAgentProvider.USER_AGENT_HEADER_NAME, userAgentProvider.provide())
        }

        val populatedRequest =
            if (headers.isNotEmpty()) request.copy(headers = headers) else request

        val response = genericNetworkClient.send(populatedRequest)

        if (response.isSuccess) {
            val responseCookieHeaders =
                response.getOrNull()?.headers?.getAll("Set-Cookie") ?: emptyList()
            val visitorId = responseCookieHeaders
                .firstOrNull { it.startsWith("cdv=") }
                ?.substringAfter("cdv=")
                ?.substringBefore(";")
            val xp = responseCookieHeaders
                .firstOrNull { it.startsWith("xp=") }
                ?.substringAfter("xp=")
                ?.substringBefore(";")

            visitorId.let {
                if (visitorIdCookie != visitorId) {
                    stringStorage.put(VISITOR_ID_COOKIE_KEY, visitorId)
                }
            }
            xp.let {
                if (xpCookie != xp) {
                    stringStorage.put(XP_COOKIE_KEY, xp)
                }
            }
        }

        return response
    }
}