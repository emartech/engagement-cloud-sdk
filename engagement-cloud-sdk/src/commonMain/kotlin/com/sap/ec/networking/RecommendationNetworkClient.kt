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
import com.sap.ec.networking.ECHeaders.CONTACT_TOKEN_HEADER
import com.sap.ec.recommendation.RecommendationConstants.SCARAB_FORCE_COHORT_HEADER
import com.sap.ec.recommendation.RecommendationConstants.SCARAB_SESSION_HEADER
import com.sap.ec.recommendation.RecommendationConstants.SCARAB_USER_PROFILE_HEADER
import com.sap.ec.recommendation.RecommendationConstants.SCARAB_VISITOR_HEADER
import io.ktor.http.Headers

internal class RecommendationNetworkClient(
    private val genericNetworkClient: NetworkClientApi,
    private val stringStorage: StringStorageApi,
    private val requestContext: RequestContextApi,
    private val userAgentProvider: UserAgentProviderApi,
    private val contactTokenRefresher: ContactTokenRefresherApi,
    private val sdkLogger: Logger
) : NetworkClientApi {
    companion object {
        private val scarabHeaders = listOf(
            SCARAB_VISITOR_HEADER,
            SCARAB_SESSION_HEADER,
            SCARAB_USER_PROFILE_HEADER,
            SCARAB_FORCE_COHORT_HEADER
        )
    }

    override suspend fun send(request: UrlRequest): Result<Response> {
        return contactTokenRefresher.executeWithTokenRefresh {
            val contactToken = requestContext.contactToken
            if (contactToken.isNullOrEmpty()) {
                sdkLogger.error("Contact token is missing")
                return@executeWithTokenRefresh Result.failure(
                    SdkException.ContactTokenNotFoundException(
                        "Contact token is missing"
                    )
                )
            }

            val headers = buildRequestHeaders(contactToken)

            val populatedRequest =
                if (headers.isNotEmpty()) request.copy(headers = headers) else request

            val response = genericNetworkClient.send(populatedRequest)

            if (response.isSuccess) {
                handleResponseHeaders(response.getOrNull()?.headers)
            }

            response
        }
    }

    private suspend fun buildRequestHeaders(contactToken: String): Map<String, String> = buildMap {
        put(CONTACT_TOKEN_HEADER, contactToken)
        put(UserAgentProvider.USER_AGENT_HEADER_NAME, userAgentProvider.provide())
        scarabHeaders.forEach { key ->
            stringStorage.get(key)?.takeIf { it.isNotEmpty() }?.let { put(key, it) }
        }
    }

    private fun handleResponseHeaders(responseHeaders: Headers?) {
        scarabHeaders.forEach { key ->
            val newValue = responseHeaders?.get(key)
            if (newValue != null && stringStorage.get(key) != newValue) {
                stringStorage.put(key, newValue)
            }
        }
    }
}
