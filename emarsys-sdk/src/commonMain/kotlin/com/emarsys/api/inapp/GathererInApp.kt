package com.emarsys.api.inapp


class GathererInApp(
    private val inAppContext: InAppApiContext,
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