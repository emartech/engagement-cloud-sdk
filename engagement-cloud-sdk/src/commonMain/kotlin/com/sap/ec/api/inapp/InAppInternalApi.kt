package com.sap.ec.api.inapp

import com.sap.ec.InternalSdkApi

@InternalSdkApi
interface InAppInternalApi {
    suspend fun pause()
    suspend fun resume()
    val isPaused: Boolean
}