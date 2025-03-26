package com.emarsys.api.push

import com.emarsys.api.push.PushCall.ClearPushToken
import com.emarsys.api.push.PushCall.RegisterPushToken
import com.emarsys.api.push.PushConstants.LAST_SENT_PUSH_TOKEN_STORAGE_KEY
import com.emarsys.api.push.PushConstants.PUSH_TOKEN_STORAGE_KEY
import com.emarsys.core.collections.dequeue
import com.emarsys.core.log.Logger
import com.emarsys.core.storage.StringStorageApi
import com.emarsys.networking.clients.push.PushClientApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

internal open class PushInternal(
    private val pushClient: PushClientApi,
    private val storage: StringStorageApi,
    private val pushContext: PushContextApi,
) : PushInstance, KoinComponent {
    private val sdkLogger: Logger by inject { parametersOf(PushInternal::class.simpleName) }

    override suspend fun registerPushToken(pushToken: String) {
        storage.put(PUSH_TOKEN_STORAGE_KEY, pushToken)
        val lastSentPushToken = storage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY)
        if (lastSentPushToken != pushToken) {
            pushClient.registerPushToken(pushToken)
            storage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, pushToken)
        }
    }

    override suspend fun clearPushToken() {
        pushClient.clearPushToken()
        storage.put(LAST_SENT_PUSH_TOKEN_STORAGE_KEY, null)
    }

    override suspend fun getPushToken(): String? {
        return storage.get(LAST_SENT_PUSH_TOKEN_STORAGE_KEY) ?: storage.get(PUSH_TOKEN_STORAGE_KEY)
    }

    override suspend fun activate() {
        pushContext.calls.dequeue { call ->
            when (call) {
                is RegisterPushToken -> pushClient.registerPushToken(call.pushToken)
                is ClearPushToken -> pushClient.clearPushToken()
                is PushCall.HandleMessageWithUserInfo -> {
                    sdkLogger.debug(
                        "PushInternal - activate",
                        "Common PushInternal: shouldn't handle message with user info: $call"
                    )
                }
            }
        }
    }
}