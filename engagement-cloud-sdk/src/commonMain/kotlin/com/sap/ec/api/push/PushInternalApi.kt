package com.sap.ec.api.push

import com.sap.ec.InternalSdkApi

@InternalSdkApi
interface PushInternalApi {
    suspend fun registerPushToken(pushToken: String)
    suspend fun clearPushToken()

    suspend fun getPushToken(): String?
}