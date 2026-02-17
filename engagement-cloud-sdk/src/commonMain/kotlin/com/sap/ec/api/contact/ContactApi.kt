package com.sap.ec.api.contact

import com.sap.ec.api.AutoRegisterable

/**
 * Interface for performing contact-related operations.
 *
 * This API allows linking, unlinking, and authenticating contacts. These operations
 * are used to associate users with the SDK for tracking and personalization purposes.
 */
interface ContactApi : AutoRegisterable {

    /**
     * Links a contact to the SDK using a contact value as the identifier.
     *
     * This operation associates the provided contact information with the currently running
     * instance of the SDK, enabling personalized tracking and messaging.
     *
     * Example usage:
     * ```kotlin
     * EngagementCloud.contact.link("1234567")
     * ```
     *
     * @param contactFieldValue The value of the contact field to link.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun link(contactFieldValue: String): Result<Unit>

    /**
     * Links a contact to the SDK using an OpenID token.
     *
     * This operation is used for linking contacts that are authenticated using OpenID Connect.
     * The OpenID token is provided by an external authentication provider.
     *
     * Example usage:
     * ```kotlin
     * EngagementCloud.contact.linkAuthenticated(3, "open_id_token_example")
     * ```
     *
     * @param openIdToken The OpenID token used for authentication.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun linkAuthenticated(openIdToken: String): Result<Unit>

    /**
     * Unlinks the currently linked contact from the SDK.
     *
     * This operation removes the association between the SDK and the contact. It can be used
     * for signing out a user.
     *
     * Example usage:
     * ```kotlin
     * EngagementCloud.contact.unlink()
     * ```
     *
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun unlink(): Result<Unit>
}