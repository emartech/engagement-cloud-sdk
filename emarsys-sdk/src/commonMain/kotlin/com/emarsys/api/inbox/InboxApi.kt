package com.emarsys.api.inbox

import com.emarsys.api.inbox.model.Message

/**
 * Interface for performing inbox-related operations.
 *
 * This API allows fetching inbox messages and message tagging functionality.
 */
interface InboxApi {

    /**
     * Fetches all inbox messages.
     *
     * This operation retrieves the list of messages available in the inbox.
     *
     * @return A [Result] containing a list of [Message] objects or an error if the operation fails.
     */
    suspend fun fetchMessages(): Result<List<Message>>

    /**
     * Adds a [tag] to a specific inbox message.
     *
     * @param tag The tag to add to the message.
     * @param messageId The ID of the message to which the tag should be added.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun addTag(tag: String, messageId: String): Result<Unit>

    /**
     * Removes a [tag] from a specific inbox message.
     *
     * @param tag The tag to remove from the message.
     * @param messageId The ID of the message from which the tag should be removed.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun removeTag(tag: String, messageId: String): Result<Unit>
}