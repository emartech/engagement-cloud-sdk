package com.sap.ec.enable.states

import com.sap.ec.api.push.PushConstants
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.state.State
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class RegisterPushTokenState(
    private val storage: StringStorageApi,
    private val sdkEventDistributor: SdkEventDistributorApi
) : State {
    override val name: String
        get() = "registerPushToken"

    override fun prepare() {
    }

    override suspend fun active(): Result<Unit> {
        val pushToken = storage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY)
        val lastSentPushToken = storage.get(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY)

        return if (pushToken != null && pushToken != lastSentPushToken) {
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.RegisterPushToken(pushToken = pushToken)
            ).await<Response>()
                .result
                .mapCatching {
                    storage.put(PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY, pushToken)
                }
        } else {
            Result.success(Unit)
        }
    }

    override fun relax() {
    }
}