package com.sap.ec.api.inapp

interface IosInAppApi {
    val isPaused:Boolean
    suspend fun pause()
    suspend fun resume()
}