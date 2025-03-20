package com.emarsys.api.push


interface PushInternalApi {
    suspend fun registerPushToken(pushToken: String)
    suspend fun clearPushToken()

    suspend fun getPushToken(): String?
}