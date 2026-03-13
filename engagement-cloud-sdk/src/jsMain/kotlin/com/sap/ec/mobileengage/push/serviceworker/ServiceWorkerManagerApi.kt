package com.sap.ec.mobileengage.push.serviceworker

import com.sap.ec.api.config.ServiceWorkerOptions
import web.serviceworker.ServiceWorkerRegistration

interface ServiceWorkerManagerApi {

    suspend fun register(): Result<ServiceWorkerRegistration>

    suspend fun unregister()

    suspend fun getServiceWorkerOptions(): ServiceWorkerOptions?

    suspend fun getServiceWorkerRegistration(): ServiceWorkerRegistration?
}