package com.emarsys.api.inapp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

class JSInApp(private val inAppApi: InAppApi, private val applicationScope: CoroutineScope) :
    JSInAppApi {
    override val isPaused: Boolean = inAppApi.isPaused

    override fun pause(): Promise<Unit> {
        return applicationScope.promise {
            inAppApi.pause().getOrThrow()
        }
    }

    override fun resume(): Promise<Unit> {
        return applicationScope.promise {
            inAppApi.resume().getOrThrow()
        }
    }

}