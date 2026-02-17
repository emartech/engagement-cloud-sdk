package com.sap.ec.tracking

import com.sap.ec.api.event.model.TrackedEvent

/**
 * Interface for performing event tracking operations.
 */
interface TrackingApi {

    /**
     * Tracks an event.
     *
     * This operation registers an event with the Engagement Cloud platform.
     *
     * Example usage:
     * ``` kotlin
     *     EngagementCloud.event.track(
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