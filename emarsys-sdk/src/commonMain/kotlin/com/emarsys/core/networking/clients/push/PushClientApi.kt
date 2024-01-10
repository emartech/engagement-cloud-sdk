package com.emarsys.core.networking.clients.push

interface PushClientApi {
    suspend fun registerPushToken(pushToken: String)
}