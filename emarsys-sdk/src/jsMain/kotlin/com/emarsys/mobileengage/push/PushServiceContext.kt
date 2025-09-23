package com.emarsys.mobileengage.push

import com.emarsys.core.device.notification.PermissionState
import web.serviceworker.ServiceWorkerRegistration

class PushServiceContext: PushServiceContextApi {

    override var registration: ServiceWorkerRegistration? = null
    override var isServiceWorkerRegistered: Boolean = (registration == null)
    override var isSubscribed: Boolean = false

    override suspend fun getPermissionState(): PermissionState {
        return registration?.pushManager?.let {
            enumValueOf<PermissionState>(it.permissionState().toString())
        } ?: PermissionState.Denied
    }
}