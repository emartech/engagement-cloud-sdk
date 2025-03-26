package com.emarsys.mobileengage.action

import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.models.ActionModel

internal interface EventActionFactoryApi: ActionFactoryApi<ActionModel> {
    override suspend fun create(action: ActionModel): Action<*>
}