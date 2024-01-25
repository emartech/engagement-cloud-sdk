package com.emarsys.networking.clients.contact

import com.emarsys.core.networking.model.Response

interface ContactTokenHandlerApi {
    fun handleContactTokens(response: Response)
}