package com.sap.ec.networking.clients.jsbridge

internal interface JsBridgeClientApi {
    suspend fun fetchJSBridge(): Result<Unit>
    suspend fun fetchServerMd5(): Result<String>
}
