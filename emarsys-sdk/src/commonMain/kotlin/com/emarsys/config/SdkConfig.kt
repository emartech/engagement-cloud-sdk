package com.emarsys.config

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


@OptIn(ExperimentalJsExport::class)
@JsExport
interface SdkConfig {
    val applicationCode: String?
    val merchantId: String?
    val sharedSecret: String?

    fun copyWith(
        applicationCode: String? = this.applicationCode,
        merchantId: String? = this.merchantId,
        sharedSecret: String? = this.sharedSecret
    ): SdkConfig
}