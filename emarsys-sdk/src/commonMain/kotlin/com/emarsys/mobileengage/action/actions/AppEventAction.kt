package com.emarsys.mobileengage.action.actions

import com.emarsys.api.AppEvent
import com.emarsys.api.oneventaction.OnEventActionInternalApi
import com.emarsys.mobileengage.action.models.AppEventActionModel

class AppEventAction(
    private val action: AppEventActionModel,
    private val onEventActionInternalApi: OnEventActionInternalApi
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        onEventActionInternalApi.events.emit(AppEvent(action.name, action.payload))
    }
}