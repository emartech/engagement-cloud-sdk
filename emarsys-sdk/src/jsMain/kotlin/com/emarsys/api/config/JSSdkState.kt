package com.emarsys.api.config

import com.emarsys.api.SdkState

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("SdkState")
object JSSdkState {
    const val ACTIVE = "active"
    const val ON_HOLD = "on_hold"
    const val UN_INITIALIZED = "un_initialized"
    const val INITIALIZED = "initialized"
}

fun SdkState.toJsSdkState(): String = when (this) {
    SdkState.Active -> JSSdkState.ACTIVE
    SdkState.OnHold -> JSSdkState.ON_HOLD
    SdkState.UnInitialized -> JSSdkState.UN_INITIALIZED
    SdkState.Initialized -> JSSdkState.INITIALIZED
}