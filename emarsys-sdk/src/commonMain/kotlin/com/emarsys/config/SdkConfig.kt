package com.emarsys.config

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


@OptIn(ExperimentalJsExport::class)
@JsExport
interface SdkConfig {
    val applicationCode: String?

    fun copyWith(
        applicationCode: String? = this.applicationCode
    ): SdkConfig
}