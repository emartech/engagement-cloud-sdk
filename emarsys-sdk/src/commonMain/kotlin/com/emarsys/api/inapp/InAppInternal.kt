package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.MutableSharedFlow

class InAppInternal(override val events: MutableSharedFlow<AppEvent>) : InAppInternalApi {
    override suspend fun pause() {
        TODO("Not yet implemented")
    }

    override suspend fun resume() {
        TODO("Not yet implemented")
    }

    override val isPaused: Boolean
        get() = TODO("Not yet implemented")

}