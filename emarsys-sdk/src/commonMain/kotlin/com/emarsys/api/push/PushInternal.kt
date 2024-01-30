package com.emarsys.api.push

import com.emarsys.api.SdkResult
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.networking.clients.push.PushClientApi

class PushInternal(
    private val pushClient: PushClientApi,
    private val storage: TypedStorageApi<String?>
) : PushInstance {

    override suspend fun registerPushToken(pushToken: String): SdkResult {
        val storedPushToken = storage.get(PUSH_TOKEN_STORAGE_KEY)
        if (storedPushToken != pushToken) {
            pushClient.registerPushToken(pushToken)
            storage.put(PUSH_TOKEN_STORAGE_KEY, pushToken)
        }
        return SdkResult.Success(Unit)
    }

    override suspend fun clearPushToken(): SdkResult {
        pushClient.clearPushToken()
        return SdkResult.Success(Unit)

    }

    override val pushToken: String?
        get() = storage.get(PUSH_TOKEN_STORAGE_KEY)


    override suspend fun activate() {
    }
}