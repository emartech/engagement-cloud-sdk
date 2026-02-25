package com.sap.ec.mobileengage.inapp.jsbridge

internal interface JsBridgeVerifierApi {
    suspend fun shouldInjectJsBridge(): Result<Boolean>
}
