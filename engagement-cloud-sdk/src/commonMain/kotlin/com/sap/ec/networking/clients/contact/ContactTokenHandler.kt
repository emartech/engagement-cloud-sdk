package com.sap.ec.networking.clients.contact

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.context.RequestContextApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.body
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

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
            currentCoroutineContext().ensureActive()
            sdkLogger.error("ContactTokenHandler - handleContactTokens", ignored)
        }
    }
}