package com.emarsys.api.push

import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.core.networking.clients.push.PushClientApi
import com.emarsys.core.storage.StorageApi

class PushInternal(
    private val pushClient: PushClientApi,
    private val storageApi: StorageApi<String>
): PushInstance {

    override suspend fun registerPushToken(pushToken: String) {
        val storedPushToken = storageApi.get(PUSH_TOKEN_STORAGE_KEY)
        if (storedPushToken != pushToken) {
            pushClient.registerPushToken(pushToken)
            storageApi.put(PUSH_TOKEN_STORAGE_KEY, pushToken)
        }
    }

    override suspend fun clearPushToken() {
        pushClient.clearPushToken()
    }

    override suspend fun activate() {
        TODO("Not yet implemented")
    }
}