package com.emarsys.api.push

internal class JSPush(private val pushApi: PushApi) :
    JSPushApi {
    override suspend fun registerPushToken(pushToken: String) {
        pushApi.registerPushToken(pushToken).getOrThrow()
    }

    override suspend fun clearPushToken() {
        pushApi.clearPushToken().getOrThrow()
    }

    override suspend fun getPushToken(): String? {
        return pushApi.getPushToken().getOrThrow()
    }
}