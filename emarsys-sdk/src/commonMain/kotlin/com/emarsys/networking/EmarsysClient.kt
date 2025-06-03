package com.emarsys.networking

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.core.providers.InstantProvider
import com.emarsys.core.networking.context.RequestContext
import com.emarsys.core.url.EmarsysUrlType
import com.emarsys.core.url.UrlFactoryApi
import com.emarsys.networking.EmarsysHeaders.CLIENT_ID_HEADER
import com.emarsys.networking.EmarsysHeaders.CLIENT_STATE_HEADER
import com.emarsys.networking.EmarsysHeaders.CONTACT_TOKEN_HEADER
import com.emarsys.networking.EmarsysHeaders.REQUEST_ORDER_HEADER
import com.emarsys.networking.EmarsysHeaders.X_CLIENT_ID_HEADER
import com.emarsys.networking.EmarsysHeaders.X_CLIENT_STATE_HEADER
import com.emarsys.networking.EmarsysHeaders.X_CONTACT_TOKEN_HEADER
import com.emarsys.networking.clients.error.ResponseErrorBody
import com.emarsys.networking.clients.event.model.SdkEvent
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.time.Duration.Companion.seconds

internal class EmarsysClient(
    private val networkClient: NetworkClientApi,
    private val requestContext: RequestContext,
    private val timestampProvider: InstantProvider,
    private val urlFactory: UrlFactoryApi,
    private val json: Json,
    private val sdkLogger: Logger,
    private val sdkEventDistributor: SdkEventDistributorApi
) : NetworkClientApi {
    private companion object {
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun send(
        request: UrlRequest,
        onNetworkError: (suspend () -> Unit)?
    ): Response {
        return refreshContactToken {
            val emarsysRequest = addEmarsysHeaders(request)
            val response = networkClient.send(emarsysRequest, onNetworkError)
            handleEmarsysResponse(response)
            handleClientState(response)
            response
        }
    }

    private suspend fun refreshContactToken(
        retryCount: Long = 0,
        callback: suspend () -> Response
    ): Response {
        val response = callback()
        return if (response.status == HttpStatusCode.Unauthorized && requestContext.refreshToken != null && retryCount < MAX_RETRY_COUNT) {
            sdkLogger.debug(
                "refreshing contact token",
                buildJsonObject {
                    put("retryCount", JsonPrimitive(retryCount))
                    put("status", JsonPrimitive(response.status.value))
                }
            )
            delay((retryCount + 1).seconds)
            val request = createRefreshContactTokenRequest()
            val refreshResponse = networkClient.send(request)
            val responseBody: RefreshTokenResponseBody = refreshResponse.body()
            requestContext.contactToken = responseBody.contactToken
            refreshContactToken(retryCount + 1, callback)
        } else {
            response
        }
    }

    private fun createRefreshContactTokenRequest() = UrlRequest(
        urlFactory.create(EmarsysUrlType.REFRESH_TOKEN),
        HttpMethod.Post,
        json.encodeToString(RefreshTokenRequestBody(requestContext.refreshToken!!)),
        mapOf(
            CLIENT_ID_HEADER to requestContext.clientId,
            CLIENT_STATE_HEADER to requestContext.clientState,
            REQUEST_ORDER_HEADER to timestampProvider.provide().toEpochMilliseconds()
        )
    )

    private suspend fun handleEmarsysResponse(response: Response) {
        if (response.status.value in HttpStatusCode.BadRequest.value..HttpStatusCode.GatewayTimeout.value) {
            val parsedBody = response.body<ResponseErrorBody>()

            val event = when (parsedBody.error.code) {
                in 1100..1199 -> SdkEvent.Internal.Sdk.ReregistrationRequired()
                in 1200..1299 -> SdkEvent.Internal.Sdk.RemoteConfigUpdateRequired()
                else -> null
            }

            sdkLogger.debug(
                "Received ${response.status.value} status code, mapped to ${event?.name ?: "unknown"} event",
            )
            event?.let {
                sdkEventDistributor.registerEvent(event)
            }
        }
    }

    private fun handleClientState(response: Response) {
        if (response.status.isSuccess()) {
            response.headers[CLIENT_STATE_HEADER.lowercase()]?.let {
                requestContext.clientState = it
            }
        }
    }

    private fun addEmarsysHeaders(request: UrlRequest): UrlRequest {
        val emarsysHeaders = mutableMapOf(
            CLIENT_ID_HEADER to requestContext.clientId,
            X_CLIENT_ID_HEADER to requestContext.clientId,
            CLIENT_STATE_HEADER to requestContext.clientState,
            X_CLIENT_STATE_HEADER to requestContext.clientState,
            CONTACT_TOKEN_HEADER to requestContext.contactToken,
            X_CONTACT_TOKEN_HEADER to requestContext.contactToken,
            REQUEST_ORDER_HEADER to timestampProvider.provide().toEpochMilliseconds()
        ).filterValues { it != null }

        val headers = request.headers?.let {
            emarsysHeaders + it
        } ?: emarsysHeaders

        return request.copy(headers = headers)
    }
}