package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSInboxApi {
    fun fetchMessages(): Promise<List<Message>>

    fun addTag(tag: String, messageId: String): Promise<Unit>

    fun removeTag(tag: String, messageId: String): Promise<Unit>
}