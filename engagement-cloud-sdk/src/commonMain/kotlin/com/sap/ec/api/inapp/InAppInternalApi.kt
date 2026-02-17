package com.sap.ec.api.inapp



interface InAppInternalApi {
    suspend fun pause()
    suspend fun resume()
    val isPaused: Boolean
}