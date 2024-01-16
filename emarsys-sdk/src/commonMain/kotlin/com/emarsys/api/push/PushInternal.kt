package com.emarsys.api.push

import com.emarsys.core.networking.clients.push.PushClientApi
import com.emarsys.core.storage.StorageApi

class PushInternal(
    private val pushClient: PushClientApi,
    private val storageApi: StorageApi<String>
): PushInstance {

    override suspend fun registerPushToken(pushToken: String) {
        val storedPushToken = storageApi.get("emsPushToken")
        if (storedPushToken != pushToken) {
            pushClient.registerPushToken(pushToken)
            storageApi.put("emsPushToken", pushToken)
        }
    }

    override suspend fun clearPushToken() {
        pushClient.clearPushToken()
    }

    override suspend fun activate() {
        TODO("Not yet implemented")
    }
}