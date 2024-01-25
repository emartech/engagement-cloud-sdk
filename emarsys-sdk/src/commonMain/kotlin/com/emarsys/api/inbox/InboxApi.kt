package com.emarsys.api.inbox

import com.emarsys.api.SdkResult

interface InboxApi {
    suspend fun fetchMessages(): SdkResult

    suspend fun addTag(tag: String, messageId: String): SdkResult

    suspend fun removeTag(tag: String, messageId: String): SdkResult
}