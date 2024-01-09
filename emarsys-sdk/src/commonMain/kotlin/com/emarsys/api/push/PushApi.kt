package com.emarsys.api.push

interface PushApi {
    suspend fun registerPushToken(pushToken: String)
    suspend fun clearPushToken()
}