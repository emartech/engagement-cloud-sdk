package com.emarsys.api.contact

import com.emarsys.api.AutoRegisterable

/**
 * Interface for performing contact-related operations.
 *
 * This API allows linking, unlinking, and authenticating contacts. These operations
 * are used to associate users with the SDK for tracking and personalization purposes.
 */
interface ContactApi : AutoRegisterable {

    /**
     * Links a contact to the SDK using a specific contact field ID and its value.
     *
     * This operation associates the provided contact information with the currently running
     * instance of the SDK, enabling personalized tracking and messaging.
     *
     * Example usage:
     * ```kotlin
     * Emarsys.contact.linkContact(3, "test@example.com")
     * ```
     *
     * @param contactFieldId The ID of the contact field.
     * @param contactFieldValue The value of the contact field to link.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun linkContact(contactFieldId: Int, contactFieldValue: String): Result<Unit>

    /**
     * Links a contact to the SDK using a specific contact field ID and OpenID token.
     *
     * This operation is used for linking contacts that are authenticated using OpenID Connect.
     * The OpenID token is provided by an external authentication provider.
     *
     * Example usage:
     * ```kotlin
     * Emarsys.contact.linkAuthenticatedContact(3, "open_id_token_example")
     * ```
     *
     * @param contactFieldId The ID of the contact field to link (e.g., email, phone number).
     * @param openIdToken The OpenID token used for authentication.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String): Result<Unit>

    /**
     * Unlinks the currently linked contact from the SDK.
     *
     * This operation removes the association between the SDK and the contact. It can be used
     * for signing out a user.
     *
     * Example usage:
     * ```kotlin
     * Emarsys.contact.unlinkContact()
     * ```
     *
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun unlinkContact(): Result<Unit>
}