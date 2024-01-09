package com.emarsys.networking.ktor.plugin

import com.emarsys.session.SessionContext
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.Sender
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

class EmarsysAuthPlugin private constructor(val sessionContext: SessionContext){

    companion object: HttpClientPlugin<EmarsysAuthPluginConfig, EmarsysAuthPlugin> {

        private const val contactTokenHeader = "X-Contact-Token"
        private const val clientIdHeader = "X-Client-Id"
        private const val requestOrderHeader = "X-Request-Order"
        private const val contactTokenRefreshUrl = "https://me-client.gservice.emarsys.net/v3/contact-token"
        private const val oneSec = 1000L
        private const val maxRetryCount = 3

        override val key: AttributeKey<EmarsysAuthPlugin> = AttributeKey("EmarsysAuthPlugin")

        override fun prepare(block: EmarsysAuthPluginConfig.() -> Unit): EmarsysAuthPlugin {
            val config = EmarsysAuthPluginConfig().apply(block)
            return EmarsysAuthPlugin(config.sessionContext ?: throw IllegalArgumentException("SessionContext must be set up!"))
        }

        override fun install(plugin: EmarsysAuthPlugin, scope: HttpClient) {
            scope.plugin(HttpSend).intercept { request ->
                send(this, request, plugin, scope)
            }
        }

        private suspend fun send(sender: Sender, request: HttpRequestBuilder, plugin: EmarsysAuthPlugin, client: HttpClient, callCount: Int = 0): HttpClientCall {
            request.extendWithContactHeader(plugin.sessionContext)
            val call = sender.execute(request)
            call.response.run {
                return if(status == HttpStatusCode.Unauthorized && callCount < maxRetryCount) {
                    refreshContactToken(client, plugin.sessionContext)
                    delay((callCount + 1) * oneSec)
                    send(sender, request, plugin, client, callCount + 1)
                } else {
                    call
                }
            }
        }

        private suspend fun refreshContactToken(client: HttpClient, sessionContext: SessionContext?) {
            if (sessionContext?.clientId != null && sessionContext.refreshToken != null) {
                val response = client.request {
                    url(contactTokenRefreshUrl)
                    header(clientIdHeader, sessionContext.clientId)
                    header(requestOrderHeader, Clock.System.now().toString())
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    setBody(RefreshTokenRequestBody(sessionContext.refreshToken as String))
                }
                val responseBody = response.body<RefreshTokenResponseBody>()
                sessionContext.contactToken = responseBody.contactToken
            }
        }
        private fun HttpRequestBuilder.extendWithContactHeader(sessionContext: SessionContext?) {
            sessionContext?.contactToken?.let {
                this.headers.remove(contactTokenHeader)
                this.headers.append(contactTokenHeader, it)
            }
        }

    }

}
