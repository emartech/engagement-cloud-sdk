package com.emarsys.networking

import com.emarsys.networking.model.Response
import com.emarsys.networking.model.UrlRequest

interface NetworkClientApi {
    suspend fun send(request: UrlRequest): Response
}