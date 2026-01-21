package com.emarsys.api.setup

import com.emarsys.JsApiConfig

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsSetupApi {
    suspend fun enableTracking(config: JsApiConfig)
    suspend fun disableTracking()

    suspend fun isEnabled(): Boolean
}