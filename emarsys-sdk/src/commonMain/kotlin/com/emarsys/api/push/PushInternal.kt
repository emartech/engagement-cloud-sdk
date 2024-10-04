package com.emarsys.api.push

import com.emarsys.api.generic.ApiContext
import com.emarsys.api.push.PushCall.ClearPushToken
import com.emarsys.api.push.PushCall.RegisterPushToken
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.core.collections.dequeue
import com.emarsys.core.storage.TypedStorageApi
import com.emarsys.networking.clients.push.PushClientApi

open class PushInternal(
    private val pushClient: PushClientApi,
    private val storage: TypedStorageApi<String?>,
    private val pushContext: ApiContext<PushCall>,
) : PushInstance {

    override suspend fun registerPushToken(pushToken: String) {
        val storedPushToken = storage.get(PUSH_TOKEN_STORAGE_KEY)
        if (storedPushToken != pushToken) {
            pushClient.registerPushToken(pushToken)
            storage.put(PUSH_TOKEN_STORAGE_KEY, pushToken)
        }
    }

    override suspend fun clearPushToken() {
        pushClient.clearPushToken()
        storage.put(PUSH_TOKEN_STORAGE_KEY, null)
    }

    override val pushToken: String?
        get() = storage.get(PUSH_TOKEN_STORAGE_KEY)


    override suspend fun activate() {
        pushContext.calls.dequeue { call ->
            when (call) {
                is RegisterPushToken -> pushClient.registerPushToken(call.pushToken)
                is ClearPushToken -> pushClient.clearPushToken()
            }
        }
    }
}