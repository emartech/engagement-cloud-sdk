package com.sap.ec.mobileengage.push

import com.sap.ec.api.Activatable
import com.sap.ec.core.device.notification.PermissionState


internal interface JsPushInstance : Activatable {
    suspend fun subscribe(): Result<Unit>
    suspend fun unsubscribe(): Result<Unit>
    suspend fun isSubscribed(): Boolean
    suspend fun getPermissionState(): PermissionState
}