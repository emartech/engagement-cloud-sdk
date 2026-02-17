package com.sap.ec.mobileengage.push

import web.serviceworker.ServiceWorkerRegistration

interface PushServiceContextApi {
    var registration: ServiceWorkerRegistration?
    var isServiceWorkerRegistered: Boolean
    var isSubscribed: Boolean
    suspend fun getPermissionState(): com.sap.ec.core.device.notification.PermissionState
}