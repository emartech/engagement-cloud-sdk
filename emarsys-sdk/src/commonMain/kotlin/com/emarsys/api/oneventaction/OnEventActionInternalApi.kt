package com.emarsys.api.oneventaction

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.MutableSharedFlow

interface OnEventActionInternalApi {
    val events: MutableSharedFlow<AppEvent>
}