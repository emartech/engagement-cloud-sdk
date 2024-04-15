package com.emarsys.api.push

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.MutableSharedFlow

interface PushInternalApi {
    suspend fun registerPushToken(pushToken: String)
    suspend fun clearPushToken()

    val pushToken: String?

    val notificationEvents: MutableSharedFlow<AppEvent>
}