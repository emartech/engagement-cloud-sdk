package com.sap.ec.api.inapp

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi

internal class GathererInApp(
    private val inAppConfig: InAppConfigApi,
    private val threadSafePersistentStore: ThreadSafePersistentStoreApi<InAppCall>,
) : InAppInstance {
    override val isPaused: Boolean
        get() = inAppConfig.inAppDnd

    override suspend fun pause() {
        threadSafePersistentStore.add(InAppCall.Pause())
    }

    override suspend fun resume() {
        threadSafePersistentStore.add(InAppCall.Resume())
    }

    override suspend fun activate() {}
}