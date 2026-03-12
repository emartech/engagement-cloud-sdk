package com.sap.ec.mobileengage.push

import com.sap.ec.core.device.notification.PermissionState
import web.serviceworker.ServiceWorkerRegistration

internal interface PushServiceApi {
    suspend fun subscribe(): Result<String?>
    suspend fun unsubscribe(): Result<Unit>
    suspend fun getPermissionState(): PermissionState
    suspend fun getServiceWorkerRegistration(): ServiceWorkerRegistration?
}