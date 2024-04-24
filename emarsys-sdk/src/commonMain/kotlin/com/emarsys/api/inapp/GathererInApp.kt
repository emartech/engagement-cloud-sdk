package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.MutableSharedFlow

class GathererInApp(
    private val inAppContext: InAppApiContext,
    override val events: MutableSharedFlow<AppEvent>
) : InAppInstance {
    override val isPaused: Boolean
        get() = inAppContext.inAppDnd

    override suspend fun pause() {
        inAppContext.calls.add(InAppCall.Pause())
    }

    override suspend fun resume() {
        inAppContext.calls.add(InAppCall.Resume())
    }

    override suspend fun activate() {}
}