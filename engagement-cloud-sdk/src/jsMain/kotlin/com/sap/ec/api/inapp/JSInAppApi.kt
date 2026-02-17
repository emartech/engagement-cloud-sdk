package com.sap.ec.api.inapp

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSInAppApi {
    val isPaused:Boolean
    suspend fun pause()
    suspend fun resume()
}