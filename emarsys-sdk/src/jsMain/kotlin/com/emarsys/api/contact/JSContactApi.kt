package com.emarsys.api.contact

import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSContactApi {
    fun link(contactFieldValue: String): Promise<Unit>
    fun linkAuthenticated(openIdToken: String): Promise<Unit>
    fun unlink(): Promise<Unit>
}