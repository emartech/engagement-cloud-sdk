package com.emarsys.api.push

import com.emarsys.api.AppEvent
import com.emarsys.api.SdkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface PushInternalApi {
    suspend fun registerPushToken(pushToken: String): SdkResult
    suspend fun clearPushToken(): SdkResult

    val pushToken: String?

    val notificationEvents: MutableSharedFlow<AppEvent>
}