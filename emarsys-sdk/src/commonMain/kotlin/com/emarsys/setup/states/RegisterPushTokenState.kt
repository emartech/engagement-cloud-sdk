package com.emarsys.setup.states

import com.emarsys.api.push.PushConstants
import com.emarsys.core.state.State
import com.emarsys.core.storage.StorageApi
import com.emarsys.networking.clients.push.PushClientApi

class RegisterPushTokenState(
    private val pushClient: PushClientApi,
    private val storage: StorageApi<String?>
) : State {
    override val name: String
        get() = "registerPushToken"

    override fun prepare() {
    }

    override suspend fun active() {
        val pushToken = storage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY)
        val lastSentPushToken = storage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY)

        if (pushToken != null && pushToken != lastSentPushToken) {
            pushClient.registerPushToken(pushToken)
            storage.put(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY, pushToken)
        }
    }

    override fun relax() {
    }
}