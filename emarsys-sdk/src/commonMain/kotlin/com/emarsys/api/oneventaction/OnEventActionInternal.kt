package com.emarsys.api.oneventaction

import com.emarsys.api.AppEvent

import kotlinx.coroutines.flow.MutableSharedFlow


class OnEventActionInternal : OnEventActionInternalApi {

    override val events = MutableSharedFlow<AppEvent>(replay = 100) // TODO set replay

}