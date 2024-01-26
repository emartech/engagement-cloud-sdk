package com.emarsys.api.push

import com.emarsys.api.SdkResult

interface PushApi {
    suspend fun registerPushToken(pushToken: String): SdkResult
    suspend fun clearPushToken(): SdkResult

    val pushToken: String?

}