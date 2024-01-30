package com.emarsys.api.push

import com.emarsys.api.SdkResult
import com.emarsys.api.generic.ApiContext
import com.emarsys.core.storage.TypedStorageApi

class PushGatherer(private val context: ApiContext<PushCall>, private val storage: TypedStorageApi<String?>) : PushInstance {
    override suspend fun registerPushToken(pushToken: String): SdkResult {
        context.calls.add(PushCall.SetPushToken(pushToken))
        return SdkResult.Success(Unit)
    }

    override suspend fun clearPushToken(): SdkResult {
        context.calls.add(PushCall.ClearPushToken())
        return SdkResult.Success(Unit)

    }

    override val pushToken: String?
        get() = storage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY)


    override suspend fun activate() {
    }
}