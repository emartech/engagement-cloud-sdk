package com.emarsys.api.push

import com.emarsys.api.generic.ApiContext

class GathererPush(private val context: ApiContext<PushCall>) : PushInstance {
    override suspend fun setPushToken(pushToken: String) {
        context.calls.add(PushCall.SetPushToken(pushToken))
    }

    override suspend fun clearPushToken() {
        context.calls.add(PushCall.ClearPushToken())
    }

    override suspend fun activate() {
    }
}