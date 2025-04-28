package com.emarsys.api.contact

import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSContactApi {
    fun link(contactFieldId: Int, contactFieldValue: String): Promise<Any>
    fun linkAuthenticated(contactFieldId: Int, openIdToken: String): Promise<Any>
    fun unlink(): Promise<Any>
}