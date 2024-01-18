package com.emarsys.networking

import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.networking.EmarsysHeaders.CLIENT_ID_HEADER
import com.emarsys.networking.EmarsysHeaders.CLIENT_STATE_HEADER
import com.emarsys.networking.EmarsysHeaders.CONTACT_TOKEN_HEADER
import com.emarsys.networking.EmarsysHeaders.REQUEST_ORDER_HEADER
import com.emarsys.providers.Provider
import com.emarsys.session.SessionContext
import com.emarsys.url.EmarsysUrlType
import com.emarsys.url.UrlFactoryApi
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

class EmarsysClient(
    private val networkClient: NetworkClientApi,
    private val sessionContext: SessionContext,
    private val timestampProvider: Provider<Instant>,
    private val urlFactory: UrlFactoryApi,
    private val json: Json
) : NetworkClientApi {
    private companion object {
        private const val MAX_RETRY_COUNT = 3
    }

    override suspend fun send(request: UrlRequest): Response {
        return refreshContactToken {
            val emarsysRequest = addEmarsysHeaders(request)
            val response = networkClient.send(emarsysRequest)
            handleClientState(response)
            response
        }
    }

    private suspend fun refreshContactToken(
        retryCount: Long = 0,
        callback: suspend () -> Response
    ): Response {
        val response = callback()
        return if (response.status == HttpStatusCode.Unauthorized && sessionContext.refreshToken != null && retryCount < MAX_RETRY_COUNT) {
            delay((retryCount + 1).seconds)
            val request = createRefreshContactTokenRequest()
            val refreshResponse = networkClient.send(request)
            val responseBody: RefreshTokenResponseBody = refreshResponse.body()
            sessionContext.contactToken = responseBody.contactToken
            refreshContactToken(retryCount + 1, callback)
        } else {
            response
        }
    }

    private fun createRefreshContactTokenRequest() = UrlRequest(
        urlFactory.create(EmarsysUrlType.REFRESH_TOKEN),
        HttpMethod.Post,
        json.encodeToString(RefreshTokenRequestBody(sessionContext.refreshToken!!)),
        mapOf(
            CLIENT_ID_HEADER to sessionContext.clientId,
            CLIENT_STATE_HEADER to sessionContext.clientState,
            REQUEST_ORDER_HEADER to timestampProvider.provide().toString()
        )
    )

    private fun handleClientState(response: Response) {
        if (response.status.isSuccess()) {
            response.headers[CLIENT_STATE_HEADER.lowercase()]?.let {
                sessionContext.clientState = it
            }
        }
    }

    private fun addEmarsysHeaders(request: UrlRequest): UrlRequest {
        val emarsysHeaders = mutableMapOf(
            CLIENT_ID_HEADER to sessionContext.clientId,
            CLIENT_STATE_HEADER to sessionContext.clientState,
            CONTACT_TOKEN_HEADER to sessionContext.contactToken,
            REQUEST_ORDER_HEADER to timestampProvider.provide().toString()
        ).filterValues { it != null }

        request.headers?.let {
            emarsysHeaders + it
        }
        return request.copy(headers = emarsysHeaders)
    }
}