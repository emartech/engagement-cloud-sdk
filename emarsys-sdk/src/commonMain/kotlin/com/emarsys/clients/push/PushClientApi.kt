package com.emarsys.clients.push

interface PushClientApi {
    suspend fun registerPushToken(pushToken: String)
}