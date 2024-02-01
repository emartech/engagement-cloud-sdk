package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface InAppInternalApi {
    suspend fun pause()
    suspend fun resume()
    val isPaused: Boolean
    val events: MutableSharedFlow<AppEvent>
}