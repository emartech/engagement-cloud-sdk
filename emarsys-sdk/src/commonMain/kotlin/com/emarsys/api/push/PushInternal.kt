package com.emarsys.api.push

import com.emarsys.clients.push.PushClientApi

class PushInternal(private val pushClient: PushClientApi) : PushInstance {
    override suspend fun setPushToken(pushToken: String) {
        TODO("Not yet implemented")
    }

    override suspend fun clearPushToken() {
        TODO("Not yet implemented")
    }

    override suspend fun activate() {
        TODO("Not yet implemented")
    }
}