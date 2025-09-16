package com.emarsys.enable.states

import com.emarsys.api.push.PushConstants
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.state.State
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.event.SdkEvent
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