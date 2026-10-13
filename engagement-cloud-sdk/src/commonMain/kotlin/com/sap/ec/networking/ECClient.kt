package com.sap.ec.networking

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.networking.model.body
import com.sap.ec.event.SdkEvent
import com.sap.ec.networking.ECHeaders.CLIENT_ID_HEADER
import com.sap.ec.networking.ECHeaders.CLIENT_STATE_HEADER
import com.sap.ec.networking.ECHeaders.CONTACT_TOKEN_HEADER
import com.sap.ec.networking.clients.error.ResponseErrorBody
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

internal class ECClient(
    private val networkClient: NetworkClientApi,
    private val requestContext: RequestContextApi,
    private val contactTokenRefresher: ContactTokenRefresherApi,
    private val sdkLogger: Logger,
    private val sdkEventDistributor: SdkEventDistributorApi
) : NetworkClientApi {

    override suspend fun send(request: UrlRequest): Result<Response> {
        return contactTokenRefresher.executeWithTokenRefresh {
            val ecRequest = addEcHeaders(request)
            val response = networkClient.send(ecRequest)
            response.getOrNull()?.let {
                handleEcResponse(it)
                handleClientState(it)
            }
            response
        }
    }

    private suspend fun handleEcResponse(response: Response) {
        if (response.status.value in HttpStatusCode.BadRequest.value..HttpStatusCode.GatewayTimeout.value) {
            val parsedBody = response.body<ResponseErrorBody>()

            val event = try {
                when (parsedBody.error.code.toInt()) {
                    in 1100..1199 -> SdkEvent.Internal.Sdk.ReregistrationRequired()
                    in 1200..1299 -> SdkEvent.Internal.Sdk.RemoteConfigUpdateRequired()
                    else -> null
                }
            } catch (ignored: NumberFormatException) {
                sdkLogger.debug("Response error code can't be parsed to Int. Error code value: ${parsedBody.error.code}")
                null
            }

            sdkLogger.debug(
                "Received ${response.status.value} status code, mapped to ${event ?: "unknown"} event",
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

    private fun addEcHeaders(request: UrlRequest): UrlRequest {
        val ecHeaders = mutableMapOf(
            CLIENT_ID_HEADER to requestContext.clientId,
            CLIENT_STATE_HEADER to requestContext.clientState,
            CONTACT_TOKEN_HEADER to requestContext.contactToken,
        ).filterValues { it != null }

        val headers = request.headers?.let {
            ecHeaders + it
        } ?: ecHeaders

        return request.copy(headers = headers)
    }
}
