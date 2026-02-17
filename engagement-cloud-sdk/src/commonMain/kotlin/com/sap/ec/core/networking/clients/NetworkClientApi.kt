package com.sap.ec.core.networking.clients

import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest

interface NetworkClientApi {
    suspend fun send(request: UrlRequest): Result<Response>
}