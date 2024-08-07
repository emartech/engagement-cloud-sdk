package com.emarsys


@OptIn(ExperimentalJsExport::class)
@JsExport
data class JsEmarsysConfig(
    override val applicationCode: String? = null,
    override val merchantId: String? = null,
    override val sharedSecret: String? = null,
    val serviceWorkerOptions: ServiceWorkerOptions? = null
) : SdkConfig

@OptIn(ExperimentalJsExport::class)
@JsExport
data class ServiceWorkerOptions(
    val applicationServerKey: String,
    val serviceWorkerPath: String,
    val serviceWorkerScope: String? = null,
)