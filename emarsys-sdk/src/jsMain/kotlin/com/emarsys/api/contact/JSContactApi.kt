package com.emarsys.api.contact

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSContactApi {
    suspend fun link(contactFieldValue: String)
    suspend fun linkAuthenticated(openIdToken: String)
    suspend fun unlink()
}