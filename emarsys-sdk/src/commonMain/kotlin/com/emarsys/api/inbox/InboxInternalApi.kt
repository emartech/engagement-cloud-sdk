package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message

interface InboxInternalApi {
    suspend fun fetchMessages(): List<Message>

    suspend fun addTag(tag: String, messageId: String)

    suspend fun removeTag(tag: String, messageId: String)
}