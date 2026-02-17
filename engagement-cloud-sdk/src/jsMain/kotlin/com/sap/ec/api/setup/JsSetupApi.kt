package com.sap.ec.api.setup

import com.sap.ec.JsApiConfig

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsSetupApi {
    suspend fun enableTracking(config: JsApiConfig)
    suspend fun disableTracking()

    suspend fun isEnabled(): Boolean
}