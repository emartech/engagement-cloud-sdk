package com.sap.ec.api.setup

import com.sap.ec.JsApiConfig

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsSetupApi {
    suspend fun enable(config: JsApiConfig)
    suspend fun disable()

    suspend fun isEnabled(): Boolean
}