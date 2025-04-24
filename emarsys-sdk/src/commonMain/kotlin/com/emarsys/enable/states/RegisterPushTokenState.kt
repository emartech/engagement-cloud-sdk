package com.emarsys.enable.states

import com.emarsys.api.push.PushConstants
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class RegisterPushTokenState(
    private val storage: StringStorageApi,
    private val sdkEventDistributor: SdkEventDistributorApi
) : State {
    override val name: String
        get() = "registerPushToken"

    override fun prepare() {
    }

    override suspend fun active() {
        val pushToken = storage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY)
        val lastSentPushToken = storage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY)

        if (pushToken != null && pushToken != lastSentPushToken) {
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.RegisterPushToken(
                    attributes = buildJsonObject {
                        put(PushConstants.PUSH_TOKEN_KEY, pushToken)
                    }
                )
            )?.await()
            storage.put(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY, pushToken)
        }
    }

    override fun relax() {
    }
}