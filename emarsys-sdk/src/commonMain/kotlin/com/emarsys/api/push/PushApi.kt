package com.emarsys.api.push

import com.emarsys.api.AppEvent
import com.emarsys.api.AutoRegisterable
import com.emarsys.api.SdkResult
import kotlinx.coroutines.flow.Flow

interface PushApi : AutoRegisterable {
    suspend fun registerPushToken(pushToken: String): SdkResult
    suspend fun clearPushToken(): SdkResult

    val pushToken: String?

    val notificationEvents: Flow<AppEvent>

}