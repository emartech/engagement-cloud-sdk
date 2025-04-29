package com.emarsys.api.inapp

interface IosInAppApi {
    val isPaused:Boolean
    suspend fun pause()
    suspend fun resume()
}