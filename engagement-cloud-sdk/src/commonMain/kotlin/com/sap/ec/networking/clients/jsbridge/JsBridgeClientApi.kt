package com.sap.ec.networking.clients.jsbridge

internal interface JsBridgeClientApi {
    suspend fun validateJSBridge(): Result<Unit>
    suspend fun fetchServerMd5(): Result<String>
}
