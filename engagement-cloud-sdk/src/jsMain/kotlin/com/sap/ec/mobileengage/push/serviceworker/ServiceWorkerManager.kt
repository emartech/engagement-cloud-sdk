package com.sap.ec.mobileengage.push.serviceworker

import com.sap.ec.api.config.ServiceWorkerOptions
import com.sap.ec.core.log.Logger
import js.promise.await
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import web.navigator.navigator
import web.serviceworker.RegistrationOptions
import web.serviceworker.ServiceWorkerRegistration
import web.serviceworker.register
import kotlin.apply
import kotlin.js.unsafeCast


// todo move all registration related stuff here
class ServiceWorkerManager(
    private val sdkLogger: Logger
) : ServiceWorkerManagerApi {

    // todo test
    override suspend fun register(serviceWorkerOptions: ServiceWorkerOptions): Result<ServiceWorkerRegistration> {
        try {
            println("Registering service worker with path: ${serviceWorkerOptions.serviceWorkerPath} and scope: ${serviceWorkerOptions.serviceWorkerScope}")
            val options = js("{}").unsafeCast<RegistrationOptions>().apply {
                serviceWorkerOptions.serviceWorkerScope?.let {
                    scope = it
                }
            }
            val registration =
                navigator.serviceWorker.register(serviceWorkerOptions.serviceWorkerPath, options)
            navigator.serviceWorker.ready.await()
            return Result.success(registration)
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            sdkLogger.error("Service worker registration failed", e)
            return Result.failure(e)
        }
    }
}