package com.emarsys.mobileengage.action

import com.emarsys.mobileengage.action.actions.Action

interface ActionFactoryApi<ActionModelType> {
    suspend fun create(actionModel: ActionModelType): Action<*>
}
