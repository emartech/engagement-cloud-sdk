package com.emarsys

import kotlinx.serialization.Serializable


@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class JsEmarsysConfig(
    override val applicationCode: String? = null,
    override val merchantId: String? = null,
    override val sharedSecret: String? = null,
    val serviceWorkerOptions: ServiceWorkerOptions? = null
) : SdkConfig {
    override fun copyWith(
        applicationCode: String?,
        merchantId: String?,
        sharedSecret: String?
    ): SdkConfig {
        return copy(
            applicationCode = applicationCode,
            merchantId = merchantId,
            sharedSecret = sharedSecret
        )
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class ServiceWorkerOptions(
    val applicationServerKey: String,
    val serviceWorkerPath: String,
    val serviceWorkerScope: String? = null,
)