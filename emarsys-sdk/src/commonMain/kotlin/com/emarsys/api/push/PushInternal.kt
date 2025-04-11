package com.emarsys.api.push

import com.emarsys.api.push.PushCall.ClearPushToken
import com.emarsys.api.push.PushCall.RegisterPushToken
import com.emarsys.api.push.PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_KEY
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.collections.dequeue
import com.emarsys.core.log.Logger
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.networking.clients.event.model.SdkEvent
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

internal open class PushInternal(
    private val storage: StringStorageApi,
    private val pushContext: PushContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger
) : PushInstance {

    override suspend fun registerPushToken(pushToken: String) {
        storage.put(PUSH_TOKEN_STORAGE_KEY, pushToken)
        val lastSentPushToken = storage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY)
        if (lastSentPushToken != pushToken) {
            sdkEventDistributor.registerAndStoreEvent(
                SdkEvent.Internal.Sdk.RegisterPushToken(
                    attributes = buildJsonObject {
                        put(PUSH_TOKEN_KEY, JsonPrimitive(pushToken))
                    })
            )
            storage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, pushToken)
        }
    }

    override suspend fun clearPushToken() {
        sdkEventDistributor.registerAndStoreEvent(SdkEvent.Internal.Sdk.ClearPushToken())
        storage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null)
    }

    override suspend fun getPushToken(): String? {
        return storage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) ?: storage.get(PUSH_TOKEN_STORAGE_KEY)
    }

    override suspend fun activate() {
        pushContext.calls.dequeue { call ->
            when (call) {
                is RegisterPushToken -> sdkEventDistributor.registerAndStoreEvent(
                    SdkEvent.Internal.Sdk.RegisterPushToken(
                        attributes = buildJsonObject {
                            put(PUSH_TOKEN_KEY, JsonPrimitive(call.pushToken))
                        }
                    )
                )
                is ClearPushToken -> sdkEventDistributor.registerAndStoreEvent(SdkEvent.Internal.Sdk.ClearPushToken())
                is PushCall.HandleSilentMessageWithUserInfo -> {
                    sdkLogger.debug(
                        "Common PushInternal: shouldn't handle silent message with user info: $call"
                    )
                }
            }
        }
    }
}