package com.emarsys.api.inbox

import com.emarsys.api.SdkResult

class Inbox : InboxApi {
    override suspend fun fetchMessages(): SdkResult {
        TODO("Not yet implemented")
    }

    override suspend fun addTag(tag: String, messageId: String): SdkResult {
        TODO("Not yet implemented")
    }

    override suspend fun removeTag(tag: String, messageId: String): SdkResult {
        TODO("Not yet implemented")
    }
}