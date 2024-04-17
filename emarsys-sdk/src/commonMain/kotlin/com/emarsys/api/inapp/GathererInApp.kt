package com.emarsys.api.inapp

import com.emarsys.api.AppEvent
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.flow.MutableSharedFlow

class GathererInApp(
    private val inAppContext: InAppContext,
    private val sdkContext: SdkContextApi,
    override val events: MutableSharedFlow<AppEvent>
) : InAppInstance {
    override val isPaused: Boolean
        get() = sdkContext.inAppDnd

    override suspend fun pause() {
        inAppContext.calls.add(InAppCall.Pause())
    }

    override suspend fun resume() {
        inAppContext.calls.add(InAppCall.Resume())
    }

    override suspend fun activate() {}
}