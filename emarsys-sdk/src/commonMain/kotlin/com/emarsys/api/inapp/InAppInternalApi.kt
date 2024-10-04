package com.emarsys.api.inapp



interface InAppInternalApi {
    suspend fun pause()
    suspend fun resume()
    val isPaused: Boolean
}