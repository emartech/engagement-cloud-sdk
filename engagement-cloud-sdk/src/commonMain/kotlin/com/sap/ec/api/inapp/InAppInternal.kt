package com.sap.ec.api.inapp

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi


internal class InAppInternal(
    private val inAppConfig: InAppConfigApi,
    private val threadSafePersistentStore: ThreadSafePersistentStoreApi<InAppCall>
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
        if (threadSafePersistentStore.items.isNotEmpty()) {
            when (threadSafePersistentStore.items.last()) {
                is InAppCall.Pause -> pause()
                is InAppCall.Resume -> resume()
            }
        }
    }
}