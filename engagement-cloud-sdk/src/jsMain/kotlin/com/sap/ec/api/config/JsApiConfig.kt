package com.sap.ec.api.config

import kotlinx.serialization.Serializable

/**
 * Configuration class for enabling the Engagement Cloud SDK on the Web.
 *
 * @property applicationCode The application code of your application.
 * @property serviceWorkerOptions Options for configuring the service worker for receiving web push messages.
 */
@Serializable
data class JsApiConfig(
    override val applicationCode: String,
    override val serviceWorkerOptions: ServiceWorkerOptions? = null
) : EngagementCloudConfig

/**
 * Configuration class for the Web Push service worker.
 *
 * @property applicationServerKey The application code of your application.
 * @property serviceWorkerPath The path where the service worker code is located.
 * @property serviceWorkerScope The scope of the service worker, which defines the range of URLs it controls.
 */
@Serializable
data class ServiceWorkerOptions(
    override val applicationServerKey: String,
    override val serviceWorkerPath: String,
    override val serviceWorkerScope: String? = null,
) : EngagementCloudCServiceWorkerOptions

/**
 * Represents the Configuration for enabling the Engagement Cloud SDK on the Web.
 *
 * @property applicationCode The application code of your application.
 * @property serviceWorkerOptions Options for configuring the service worker for receiving web push messages.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
interface EngagementCloudConfig {
    val applicationCode: String
    val serviceWorkerOptions: EngagementCloudCServiceWorkerOptions?
}

/**
 * Represents the Configuration for the Web Push service worker.
 *
 * @property applicationServerKey The application code of your application.
 * @property serviceWorkerPath The path where the service worker code is located.
 * @property serviceWorkerScope The scope of the service worker, which defines the range of URLs it controls.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("ServiceWorkerOptions")
interface EngagementCloudCServiceWorkerOptions {
    val applicationServerKey: String
    val serviceWorkerPath: String
    val serviceWorkerScope: String?
}
