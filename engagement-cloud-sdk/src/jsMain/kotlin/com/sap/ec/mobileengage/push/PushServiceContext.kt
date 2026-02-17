package com.sap.ec.mobileengage.push

import com.sap.ec.core.device.notification.PermissionState
import web.serviceworker.ServiceWorkerRegistration

class PushServiceContext : PushServiceContextApi {

    override var registration: ServiceWorkerRegistration? = null
    override var isServiceWorkerRegistered: Boolean = (registration == null)
    override var isSubscribed: Boolean = false

    override suspend fun getPermissionState(): PermissionState {
        return registration.asDynamic()?.pushManager?.let { pushManager ->
            enumValueOf<PermissionState>(pushManager.permissionState.toString())
        } ?: PermissionState.Denied
    }
}