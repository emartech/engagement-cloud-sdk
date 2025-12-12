package com.emarsys

import kotlinx.serialization.Serializable

/**
 * Configuration class for enabling the Emarsys SDK on the Web.
 *
 * @property applicationCode The application code of your application.
 * @property serviceWorkerOptions Options for configuring the service worker for receiving web push messages.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("EmarsysConfig")
data class JsApiConfig(
    val applicationCode: String? = null,
    val serviceWorkerOptions: ServiceWorkerOptions? = null
)

/**
 * Configuration class for the Web Push service worker.
 *
 * @property applicationServerKey The application code of your application.
 * @property serviceWorkerPath The path where the service worker code is located.
 * @property serviceWorkerScope The scope of the service worker, which defines the range of URLs it controls.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ServiceWorkerOptions(
    val applicationServerKey: String,
    val serviceWorkerPath: String,
    val serviceWorkerScope: String? = null,
)
