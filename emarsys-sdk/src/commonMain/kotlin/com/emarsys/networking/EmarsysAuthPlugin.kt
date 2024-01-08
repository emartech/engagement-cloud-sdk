package com.emarsys.networking

import com.emarsys.session.SessionContext
import io.ktor.client.call.body
import io.ktor.client.plugins.api.*
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Clock

val EmarsysAuthPlugin = createClientPlugin("EmarsysAuthPlugin", ::EmarsysAuthPluginConfig) {
    on(Send) { request ->
        pluginConfig.sessionContext?.contactToken?.let {
            request.headers.append("X-Contact-Token",
                it
            )
        }
        val originalCall = proceed(request)
        originalCall.response.run {
            if(status == HttpStatusCode.Unauthorized) {
                if (pluginConfig.sessionContext?.clientId != null && pluginConfig.sessionContext?.refreshToken != null) {
                    val response = client.request {
                        url("https://me-client.gservice.emarsys.net/v3/contact-token")
                        header("X-Client-Id", pluginConfig.sessionContext?.clientId)
                        header("X-Request-Order", Clock.System.now().toString())
                        setBody(RefreshTokenRequestBody(pluginConfig.sessionContext?.refreshToken as String))
                    }
                    val responseBody = response.body<RefreshTokenResponseBody>()
                    pluginConfig.sessionContext?.contactToken = responseBody.contactToken
                    request.headers.remove("X-Contact-Token")
                    request.headers.append("X-Contact-Token", pluginConfig.sessionContext?.contactToken!!)
                }
                proceed(request)
            } else {
                originalCall
            }
        }
    }
}

data class EmarsysAuthPluginConfig(
    var sessionContext: SessionContext? = null
)

data class RefreshTokenRequestBody(
    val refreshToken: String
)

data class RefreshTokenResponseBody(
    val contactToken: String
)