package com.sap.ec.mobileengage.push

import com.sap.ec.api.push.PushApi

internal interface JsPushWrapperApi : PushApi {
    suspend fun subscribe(): Result<Unit>
    suspend fun unsubscribe(): Result<Unit>
    suspend fun isSubscribed(): Boolean
    suspend fun getPermissionState(): String
}