package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.Flow

interface InAppApi {
    val isPaused:Boolean
    val events: Flow<AppEvent>
    suspend fun pause(): Result<Unit>
    suspend fun resume(): Result<Unit>

}