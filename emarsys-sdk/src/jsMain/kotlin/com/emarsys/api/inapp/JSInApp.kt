package com.emarsys.api.inapp

internal class JSInApp(private val inAppApi: InAppApi) :
    JSInAppApi {
    override val isPaused: Boolean = inAppApi.isPaused

    override suspend fun pause() {
        inAppApi.pause().getOrThrow()
    }

    override suspend fun resume() {
        inAppApi.resume().getOrThrow()
    }

}