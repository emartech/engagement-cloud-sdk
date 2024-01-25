package com.emarsys.api.oneventaction

import com.emarsys.api.AppEvent
import kotlinx.coroutines.flow.Flow

interface OnEventActionApi {
    val events: Flow<AppEvent>
}