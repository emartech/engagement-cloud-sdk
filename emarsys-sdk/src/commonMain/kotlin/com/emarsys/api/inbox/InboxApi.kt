package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message

interface InboxApi {
    suspend fun fetchMessages(): Result<List<Message>>

    suspend fun addTag(tag: String, messageId: String): Result<Unit>

    suspend fun removeTag(tag: String, messageId: String): Result<Unit>
}