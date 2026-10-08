package com.sap.ec.networking

import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.networking.model.body
import com.sap.ec.core.url.ECUrlType
import com.sap.ec.core.url.UrlFactoryApi
import com.sap.ec.networking.ECHeaders.CLIENT_ID_HEADER
import com.sap.ec.networking.ECHeaders.CLIENT_STATE_HEADER
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class ContactTokenRefresher(
    private val networkClient: NetworkClientApi,
    private val requestContext: RequestContextApi,
    private val urlFactory: UrlFactoryApi,
    private val json: Json,
    private val sdkLogger: Logger
) : ContactTokenRefresherApi {

    private companion object {
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun executeWithTokenRefresh(
        retryCount: Long,
        callback: suspend () -> Result<Response>
    ): Result<Response> {
        val originalResponse = callback()

        val exception = originalResponse.exceptionOrNull()
        return if (exception != null &&
            exception is SdkException.FailedRequestException &&
            exception.response.status == HttpStatusCode.Unauthorized &&
            requestContext.refreshToken != null &&
            retryCount < MAX_RETRY_COUNT
        ) {
            sdkLogger.debug(
                "refreshing contact token",
                buildJsonObject {
                    put("retryCount", JsonPrimitive(retryCount))
                    put("status", JsonPrimitive(exception.response.status.value))
                }
            )
            delay((retryCount + 1).seconds)
            val refreshRequest = createRefreshContactTokenRequest()
            val refreshResponse = networkClient.send(refreshRequest)
            refreshResponse.fold(
                onSuccess = { response ->
                    updateContactToken(response)
                    executeWithTokenRefresh(retryCount + 1, callback)
                },
                onFailure = {
                    originalResponse
                }
            )
        } else {
            originalResponse
        }
    }

    private fun updateContactToken(refreshTokenResponse: Response) {
        val responseBody: RefreshTokenResponseBody = refreshTokenResponse.body()
        requestContext.contactToken = responseBody.contactToken
    }

    private suspend fun createRefreshContactTokenRequest() = UrlRequest(
        urlFactory.create(ECUrlType.RefreshToken),
        HttpMethod.Post,
        json.encodeToString(RefreshTokenRequestBody(requestContext.refreshToken!!)),
        mapOf(
            CLIENT_ID_HEADER to requestContext.clientId,
            CLIENT_STATE_HEADER to requestContext.clientState,
        )
    )
}
