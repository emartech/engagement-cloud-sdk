package com.emarsys.api.setup

import com.emarsys.config.SdkConfig
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsSetupApi {
    fun enableTracking(config: SdkConfig): Promise<Unit>
    fun disableTracking(): Promise<Unit>

    fun isEnabled(): Promise<Boolean>
}