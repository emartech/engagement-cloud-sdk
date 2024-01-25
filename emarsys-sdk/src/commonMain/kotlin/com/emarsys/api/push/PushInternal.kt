package com.emarsys.api.push

import com.emarsys.api.SdkResult
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.networking.clients.push.PushClientApi
import com.emarsys.core.storage.StorageApi

class PushInternal(
    private val pushClient: PushClientApi,
    private val storageApi: StorageApi<String>
) : PushInstance {

    override suspend fun registerPushToken(pushToken: String): SdkResult {
        val storedPushToken = storageApi.get(PUSH_TOKEN_STORAGE_KEY)
        if (storedPushToken != pushToken) {
            pushClient.registerPushToken(pushToken)
            storageApi.put(PUSH_TOKEN_STORAGE_KEY, pushToken)
        }
        return SdkResult.Success(Unit)
    }

    override suspend fun clearPushToken(): SdkResult {
        pushClient.clearPushToken()
        return SdkResult.Success(Unit)

    }

    override var pushToken: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun activate() {
    }
}