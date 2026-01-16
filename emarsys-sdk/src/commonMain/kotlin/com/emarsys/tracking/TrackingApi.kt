package com.emarsys.tracking

import com.emarsys.api.event.model.TrackedEvent

/**
 * Interface for performing event tracking operations.
 */
interface TrackingApi {

    /**
     * Tracks an event.
     *
     * This operation registers an event with the Emarsys platform.
     *
     * Example usage:
     * ``` kotlin
     *     Emarsys.event.track(
     *         CustomEvent(
     *             name = "event_name",
     *             attributes = mapOf(
     *                 "key" to "value"
     *             )
     *         )
     *     )
     * ```
     *
     * @param trackedEvent The custom event or navigate event to track.
     * @return A [Result] indicating success or failure of the operation.
     */
    suspend fun track(trackedEvent: TrackedEvent): Result<Unit>
}