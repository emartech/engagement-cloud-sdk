package com.emarsys.api.push

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSPushApi {
    suspend fun registerPushToken(pushToken: String)
    suspend fun clearPushToken()
    suspend fun getPushToken(): String?
}