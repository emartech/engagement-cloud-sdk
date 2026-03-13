package com.sap.ec.mobileengage.push.serviceworker

import JsEngagementCloudSDKConfig
import com.sap.ec.api.config.ServiceWorkerOptions
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.Logger
import js.promise.await
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import web.navigator.navigator
import web.serviceworker.RegistrationOptions
import web.serviceworker.ServiceWorkerRegistration
import web.serviceworker.register
import web.serviceworker.unregister
import web.serviceworker.update
import kotlin.apply
import kotlin.js.unsafeCast


internal class ServiceWorkerManager(
    private val sdkContext: SdkContextApi,
    private val sdkLogger: Logger
) : ServiceWorkerManagerApi {

    private var serviceWorkerRegistration: ServiceWorkerRegistration? = null

    // todo test
    override suspend fun register(): Result<ServiceWorkerRegistration> {
        return try {
            val config = sdkContext.getSdkConfig() as JsEngagementCloudSDKConfig?
            config?.serviceWorkerOptions?.let { serviceWorkerOptions ->
                println("Registering service worker with path: ${serviceWorkerOptions.serviceWorkerPath} and scope: ${serviceWorkerOptions.serviceWorkerScope}")
                val options = js("{}").unsafeCast<RegistrationOptions>().apply {
                    serviceWorkerOptions.serviceWorkerScope?.let {
                        scope = it
                    }
                }
                val registration =
                    navigator.serviceWorker.register(
                        serviceWorkerOptions.serviceWorkerPath,
                        options
                    )
                navigator.serviceWorker.ready.await()
                serviceWorkerRegistration = registration
                Result.success(registration)
            } ?: Result.failure(IllegalStateException("Service worker options are not set."))
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            sdkLogger.error("Service worker registration failed", e)
            return Result.failure(e)
        }
    }

    override suspend fun unregister() {
        getServiceWorkerRegistration()?.unregister()
        serviceWorkerRegistration = null
    }

    override suspend fun getServiceWorkerOptions(): ServiceWorkerOptions? {
        val config = sdkContext.getSdkConfig() as JsEngagementCloudSDKConfig?
        println("Getting service worker options from SDK config: ${JSON.stringify(config)}")
        return config?.serviceWorkerOptions
    }

    override suspend fun getServiceWorkerRegistration(): ServiceWorkerRegistration? {
        return try {
            serviceWorkerRegistration ?: register().getOrNull()
                .also {
                    serviceWorkerRegistration = it
                    serviceWorkerRegistration?.update()
                }
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            sdkLogger.error("Failed to get service worker registration", e)
            null
        }
    }
}