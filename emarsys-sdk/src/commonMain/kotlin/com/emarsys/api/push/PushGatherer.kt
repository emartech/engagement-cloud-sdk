package com.emarsys.api.push

import com.emarsys.api.AppEvent
import com.emarsys.api.generic.ApiContext
import com.emarsys.core.storage.TypedStorageApi
import kotlinx.coroutines.flow.MutableSharedFlow

class PushGatherer(
    private val context: ApiContext<PushCall>, private val storage: TypedStorageApi<String?>,
    override val notificationEvents: MutableSharedFlow<AppEvent>
) : PushInstance {
    override suspend fun registerPushToken(pushToken: String) {
        context.calls.add(PushCall.RegisterPushToken(pushToken))
    }

    override suspend fun clearPushToken() {
        context.calls.add(PushCall.ClearPushToken())
    }

    override val pushToken: String?
        get() = storage.get(PushConstants.PUSH_TOKEN_STORAGE_KEY)

    override suspend fun activate() {
    }
}