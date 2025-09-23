package com.emarsys.mobileengage.push

import web.serviceworker.ServiceWorkerRegistration

interface PushServiceContextApi {
    var registration: ServiceWorkerRegistration?
    var isServiceWorkerRegistered: Boolean
    var isSubscribed: Boolean
    suspend fun getPermissionState(): com.emarsys.core.device.notification.PermissionState
}