package com.sap.ec.api.push

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.core.storage.StringStorageApi

internal open class PushGatherer(
    val threadSafePersistentStore: ThreadSafePersistentStoreApi<PushCall>,
    private val storage: StringStorageApi,
    private val sdkContext: SdkContextApi,
) : PushInstance {
    override suspend fun registerPushToken(pushToken: String) {
        threadSafePersistentStore.add(PushCall.RegisterPushToken(pushToken))
    }

    override suspend fun clearPushToken() {
        threadSafePersistentStore.add(PushCall.ClearPushToken(sdkContext.getSdkConfig()?.applicationCode))
    }

    override suspend fun getPushToken(): String? =
        storage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY)

    override suspend fun activate() {
    }
}