package com.emarsys.api.push

import com.emarsys.api.AppEvent
import com.emarsys.api.AutoRegisterable
import kotlinx.coroutines.flow.Flow

interface PushApi : AutoRegisterable {
    suspend fun registerPushToken(pushToken: String): Result<Unit>
    suspend fun clearPushToken(): Result<Unit>

    val pushToken: Result<String?>

    val notificationEvents: Flow<AppEvent>

}