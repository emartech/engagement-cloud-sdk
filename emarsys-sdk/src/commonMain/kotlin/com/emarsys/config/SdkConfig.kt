package com.emarsys.config

import kotlin.js.ExperimentalJsExport


@OptIn(ExperimentalJsExport::class)
interface SdkConfig {
    val applicationCode: String?

    fun copyWith(
        applicationCode: String? = this.applicationCode
    ): SdkConfig
}