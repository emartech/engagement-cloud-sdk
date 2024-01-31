package com.emarsys.api.oneventaction

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow

class OnEventAction(private val onEventActionInternal: OnEventActionInternal) : OnEventActionApi {
    override val events: Flow<AppEvent>
        get() = onEventActionInternal.events.asSharedFlow()
}