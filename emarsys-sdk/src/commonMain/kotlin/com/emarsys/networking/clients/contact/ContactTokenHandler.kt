package com.emarsys.networking.clients.contact

import com.emarsys.core.log.Logger
import com.emarsys.core.networking.context.RequestContextApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.body

internal class ContactTokenHandler(
    private val requestContext: RequestContextApi,
    private val sdkLogger: Logger
) : ContactTokenHandlerApi {

    override suspend fun handleContactTokens(response: Response) {
        try {
            val body: ContactTokenResponseBody = response.body()
            requestContext.refreshToken = body.refreshToken
            requestContext.contactToken = body.contactToken
        } catch (ignored: Exception) {
            sdkLogger.error("ContactTokenHandler - handleContactTokens", ignored)
        }
    }
}