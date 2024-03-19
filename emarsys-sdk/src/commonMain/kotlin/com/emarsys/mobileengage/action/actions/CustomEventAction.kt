package com.emarsys.mobileengage.action.actions

import com.emarsys.api.event.EventTrackerApi
import com.emarsys.api.event.model.CustomEvent
import com.emarsys.mobileengage.action.models.CustomEventActionModel

class CustomEventAction(
    private val action: CustomEventActionModel,
    private val eventTracker: EventTrackerApi
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        eventTracker.trackEvent(CustomEvent(action.name, action.payload))
    }
}
