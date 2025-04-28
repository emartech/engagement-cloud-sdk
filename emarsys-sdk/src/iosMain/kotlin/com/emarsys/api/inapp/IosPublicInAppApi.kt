package com.emarsys.api.inapp

interface IosPublicInAppApi {
    val isPaused:Boolean
    suspend fun pause()
    suspend fun resume()
}