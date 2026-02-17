package com.sap.ec.api.push

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSPushApi {
    suspend fun registerPushToken(pushToken: String)
    suspend fun clearPushToken()
    suspend fun getPushToken(): String?
}