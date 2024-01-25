package com.emarsys.api.oneventaction

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.Flow

class OnEventAction : OnEventActionApi {
    override val events: Flow<AppEvent>
        get() = TODO("Not yet implemented")
}