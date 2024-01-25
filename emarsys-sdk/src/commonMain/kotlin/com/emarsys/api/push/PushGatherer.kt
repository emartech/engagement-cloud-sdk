package com.emarsys.api.push

import com.emarsys.api.SdkResult
import com.emarsys.api.generic.ApiContext

class PushGatherer(private val context: ApiContext<PushCall>) : PushInstance {
    override suspend fun registerPushToken(pushToken: String): SdkResult {
        context.calls.add(PushCall.SetPushToken(pushToken))
        return SdkResult.Success(Unit)
    }

    override suspend fun clearPushToken(): SdkResult {
        context.calls.add(PushCall.ClearPushToken())
        return SdkResult.Success(Unit)

    }

    override var pushToken: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun activate() {
    }
}