package com.emarsys.api.inapp

/**
 * Interface for managing in-app messaging functionality.
 *
 * This API allows pausing and resuming in-app messaging. Pausing can be useful when
 * in-app messages should not be displayed, such as during critical user interactions.
 */
interface InAppApi {

    /**
     * Indicates whether in-app messaging is currently paused.
     *
     * While paused, no in-app messages will be displayed to the user.
     */
    val isPaused: Boolean

    /**
     * Pauses in-app messaging functionality.
     *
     * This operation temporarily disables the display of in-app messages. It can be used
     * to prevent interruptions during specific user interactions or workflows.
     *
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun pause(): Result<Unit>

    /**
     * Resumes in-app messaging functionality.
     *
     * This operation re-enables the display of in-app messages.
     *
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun resume(): Result<Unit>

}