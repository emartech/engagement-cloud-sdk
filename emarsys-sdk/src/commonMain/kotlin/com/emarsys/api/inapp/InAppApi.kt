package com.emarsys.api.inapp


interface InAppApi {
    val isPaused:Boolean
    suspend fun pause(): Result<Unit>
    suspend fun resume(): Result<Unit>

}