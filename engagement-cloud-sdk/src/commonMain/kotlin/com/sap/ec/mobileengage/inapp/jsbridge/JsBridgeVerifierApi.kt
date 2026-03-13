package com.sap.ec.mobileengage.inapp.jsbridge

internal interface JsBridgeVerifierApi {
    suspend fun verifyJsBridge(): Result<Unit>
}
