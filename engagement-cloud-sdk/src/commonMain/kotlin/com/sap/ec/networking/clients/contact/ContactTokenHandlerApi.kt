package com.sap.ec.networking.clients.contact

import com.sap.ec.core.networking.model.Response

internal interface ContactTokenHandlerApi {
    suspend fun handleContactTokens(response: Response)
}