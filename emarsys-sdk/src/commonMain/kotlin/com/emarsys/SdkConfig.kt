package com.emarsys

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


@OptIn(ExperimentalJsExport::class)
@JsExport
interface SdkConfig {
    val applicationCode: String?
    val merchantId: String?
    val sharedSecret: String?
}