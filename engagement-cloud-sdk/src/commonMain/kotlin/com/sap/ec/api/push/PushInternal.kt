package com.sap.ec.api.push

import com.sap.ec.api.push.PushCall.ClearPushToken
import com.sap.ec.api.push.PushCall.RegisterPushToken
import com.sap.ec.api.push.PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY
import com.sap.ec.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.collections.dequeue
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal open class PushInternal(
    private val storage: StringStorageApi,
    private val pushContext: PushContextApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkContext: SdkContextApi,
    private val sdkLogger: Logger
) : PushInstance {

    override suspend fun registerPushToken(pushToken: String) {
        storage.put(PUSH_TOKEN_STORAGE_KEY, pushToken)
        val lastSentPushToken = storage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY)
        if (lastSentPushToken != pushToken) {
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.RegisterPushToken(
                    pushToken = pushToken
                )
            ).await<Response>()
                .result
                .fold(
                    onSuccess = {
                        storage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, pushToken)
                    },
                    onFailure = {
                        sdkLogger.error("Register push token failed", it)
                    }
                )
        } else {
            sdkLogger.debug("Push token hasn't changed")
        }
    }

    override suspend fun clearPushToken() {
        sdkEventDistributor.registerEvent(SdkEvent.Internal.Sdk.ClearPushToken(applicationCode = sdkContext.config?.applicationCode))
        storage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null)
    }

    override suspend fun getPushToken(): String? {
        return storage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) ?: storage.get(PUSH_TOKEN_STORAGE_KEY)
    }

    override suspend fun activate() {
        pushContext.calls.dequeue { call ->
            when (call) {
                is RegisterPushToken -> sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.RegisterPushToken(
                        pushToken = call.pushToken
                    )
                )

                is ClearPushToken -> sdkEventDistributor.registerEvent(
                    SdkEvent.Internal.Sdk.ClearPushToken(
                        applicationCode = call.applicationCode
                    )
                )

                is PushCall.HandleSilentMessageWithUserInfo -> {
                    sdkLogger.debug(
                        "Common PushInternal: shouldn't handle silent message with user info: $call"
                    )
                }
            }
        }
    }
}