package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.Flow

interface InAppApi {
    suspend fun pause()
    suspend fun resume()
    val isPaused:Boolean
    val events: Flow<AppEvent>

}