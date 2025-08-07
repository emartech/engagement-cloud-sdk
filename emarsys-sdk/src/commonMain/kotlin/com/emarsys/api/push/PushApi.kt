package com.emarsys.api.push

import com.emarsys.api.AutoRegisterable

/**
 * Interface for managing push tokens.
 *
 * This API allows registering, clearing, and retrieving the push token of the the application.
 */
interface PushApi : AutoRegisterable {

    /**
     * Registers a push token.
     *
     * This operation stores the provided push token and ensures it is sent to the backend.
     * If the token has already been sent, it avoids redundant updates.
     *
     * @param pushToken The push token to register.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun registerPushToken(pushToken: String): Result<Unit>

    /**
     * Clears the currently registered push token.
     *
     * This operation removes the association between the SDK and the push token, effectively
     * disabling push notifications for the application.
     *
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun clearPushToken(): Result<Unit>

    /**
     * Retrieves the currently registered push token.
     *
     * @return A [Result] containing the push token if available, or `null` if no token is registered.
     */
    suspend fun getPushToken(): Result<String?>
}