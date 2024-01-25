package com.emarsys.networking.clients.contact

import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.body
import com.emarsys.session.SessionContext

class ContactTokenHandler(private val sessionContext: SessionContext): ContactTokenHandlerApi {

    override fun handleContactTokens(response: Response) {
        try {
            val body: ContactTokenResponseBody = response.body()
            sessionContext.refreshToken = body.refreshToken
            sessionContext.contactToken = body.contactToken
        } catch (ignored: Exception) {
        }
    }
}