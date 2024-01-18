package com.emarsys.networking.clients.push

interface PushClientApi {
    suspend fun registerPushToken(pushToken: String)
    suspend fun clearPushToken()
}