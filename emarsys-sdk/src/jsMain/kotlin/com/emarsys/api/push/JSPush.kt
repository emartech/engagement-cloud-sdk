package com.emarsys.api.push

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

class JSPush(private val pushApi: PushApi, private val applicationScope: CoroutineScope) :
    JSPushApi {
    override fun registerPushToken(pushToken: String): Promise<Unit> {
        return applicationScope.promise {
            pushApi.registerPushToken(pushToken).getOrThrow()
        }
    }

    override fun clearPushToken(): Promise<Unit> {
        return applicationScope.promise { pushApi.clearPushToken().getOrThrow() }
    }

    override fun getPushToken(): Promise<String?> {
        return applicationScope.promise { pushApi.getPushToken().getOrThrow() }
    }
}