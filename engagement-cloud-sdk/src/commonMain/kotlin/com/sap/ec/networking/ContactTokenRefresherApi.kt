package com.sap.ec.networking

import com.sap.ec.core.networking.model.Response

internal interface ContactTokenRefresherApi {
    suspend fun executeWithTokenRefresh(
        retryCount: Long = 0,
        callback: suspend () -> Result<Response>
    ): Result<Response>
}
