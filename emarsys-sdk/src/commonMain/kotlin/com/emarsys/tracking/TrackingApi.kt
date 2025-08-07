package com.emarsys.tracking

import com.emarsys.api.event.model.CustomEvent

/**
 * Interface for performing event tracking operations.
 */
interface TrackingApi {

    /**
     * Tracks a custom event.
     *
     * This operation registers a custom event with the Emarsys platform.
     *
     * Example usage:
     * ``` kotlin
     *     Emarsys.tracking.trackCustomEvent(
     *         CustomEvent(
     *             name = "event_name",
     *             attributes = mapOf(
     *                 "key" to "value"
     *             )
     *         )
     *     )
     * ```
     *
     * @param event The custom event to track.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun trackCustomEvent(event: CustomEvent): Result<Unit>
}