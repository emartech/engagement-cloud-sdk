package com.emarsys.networking.clients.contact

import com.emarsys.core.networking.model.Response

interface ContactTokenHandlerApi {
    suspend fun handleContactTokens(response: Response)
}