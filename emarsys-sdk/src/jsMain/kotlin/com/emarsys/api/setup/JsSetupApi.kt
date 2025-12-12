package com.emarsys.api.setup

import com.emarsys.JsApiConfig
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsSetupApi {
    fun enableTracking(config: JsApiConfig): Promise<Unit>
    fun disableTracking(): Promise<Unit>

    fun isEnabled(): Promise<Boolean>
}