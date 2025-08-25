package com.emarsys.core.networking.clients

import com.emarsys.core.networking.model.Response
import com.emarsys.core.networking.model.UrlRequest

interface NetworkClientApi {
    suspend fun send(request: UrlRequest): Result<Response>
}