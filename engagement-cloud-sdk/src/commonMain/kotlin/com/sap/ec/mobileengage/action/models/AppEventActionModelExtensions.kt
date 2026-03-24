package com.sap.ec.mobileengage.action.models

import com.sap.ec.api.event.model.EventSource

internal fun BasicAppEventActionModel.addSource(source: EventSource): BasicAppEventActionModel {
    return copy(source = source)
}

internal fun PresentableAppEventActionModel.addSource(source: EventSource): PresentableAppEventActionModel {
    return copy(source = source)
}

internal fun <T : ActionModel> List<T>.addAppEventSource(source: EventSource): List<T> {
    return map { action ->
        when (action) {
            is BasicAppEventActionModel -> action.addSource(source) as T
            is PresentableAppEventActionModel -> action.addSource(source) as T
            else -> action
        }
    }
}
