package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow

class InApp(private val inAppInternal: InAppInternalApi) : InAppApi {
    override suspend fun pause() {
        inAppInternal.pause()
    }

    override suspend fun resume() {
        inAppInternal.resume()
    }

    override val isPaused: Boolean
        get() = inAppInternal.isPaused

    override val events: Flow<AppEvent>
        get() = inAppInternal.events.asSharedFlow()
}