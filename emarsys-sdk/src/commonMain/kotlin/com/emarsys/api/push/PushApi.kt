package com.emarsys.api.push

import com.emarsys.api.AutoRegisterable

interface PushApi : AutoRegisterable {
    suspend fun registerPushToken(pushToken: String): Result<Unit>
    suspend fun clearPushToken(): Result<Unit>

    val pushToken: Result<String?>
}