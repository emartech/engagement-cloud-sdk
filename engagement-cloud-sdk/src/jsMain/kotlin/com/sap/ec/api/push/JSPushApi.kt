package com.sap.ec.api.push

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSPushApi {
    suspend fun subscribe()
    suspend fun unsubscribe()
    suspend fun isSubscribed(): Boolean
    suspend fun getPermissionState(): String
}