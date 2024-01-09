package com.emarsys.api.push

interface PushApi {
    suspend fun setPushToken(pushToken: String)
    suspend fun clearPushToken()
}