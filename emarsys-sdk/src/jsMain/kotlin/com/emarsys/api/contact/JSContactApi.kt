package com.emarsys.api.contact

import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSContactApi {
    fun link(contactFieldId: Int, contactFieldValue: String): Promise<Unit>
    fun linkAuthenticated(contactFieldId: Int, openIdToken: String): Promise<Unit>
    fun unlink(): Promise<Unit>
}