package com.emarsys.mobileengage.action.actions

import com.emarsys.mobileengage.action.models.AppEventActionModel
import com.emarsys.mobileengage.events.SdkEvent
import com.emarsys.mobileengage.events.SdkEventSource
import kotlinx.coroutines.flow.MutableSharedFlow

class AppEventAction(
    private val action: AppEventActionModel,
    private val sdkEvents: MutableSharedFlow<SdkEvent>
) : Action<SdkEventSource> {
    override suspend fun invoke(value: SdkEventSource?) {
        value?.let {
            sdkEvents.emit(SdkEvent(value, action.name, action.payload))
        }
    }
}