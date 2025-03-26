package com.emarsys.api.inapp

internal class GathererInApp(
    private val inAppContext: InAppContextApi,
    private val inAppConfig: InAppConfigApi,
) : InAppInstance {
    override val isPaused: Boolean
        get() = inAppConfig.inAppDnd

    override suspend fun pause() {
        inAppContext.calls.add(InAppCall.Pause())
    }

    override suspend fun resume() {
        inAppContext.calls.add(InAppCall.Resume())
    }

    override suspend fun activate() {}
}