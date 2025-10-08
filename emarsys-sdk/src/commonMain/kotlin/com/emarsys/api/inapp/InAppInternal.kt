package com.emarsys.api.inapp


internal class InAppInternal(
    private val inAppConfig: InAppConfigApi,
    private val inAppContext: InAppContextApi
) : InAppInstance {
    override suspend fun pause() {
        inAppConfig.inAppDnd = true
    }

    override suspend fun resume() {
        inAppConfig.inAppDnd = false
    }

    override val isPaused: Boolean
        get() = inAppConfig.inAppDnd

    override suspend fun activate() {
        if(inAppContext.calls.isNotEmpty()){
            when(inAppContext.calls.last()) {
                is InAppCall.Pause -> pause()
                is InAppCall.Resume -> resume()
            }
        }
    }
}