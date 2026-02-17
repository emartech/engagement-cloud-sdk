package com.sap.ec.mobileengage.action

import com.sap.ec.mobileengage.action.actions.Action
import com.sap.ec.mobileengage.action.models.ActionModel

internal interface PushActionFactoryApi: ActionFactoryApi<ActionModel> {
    override suspend fun create(actionModel: ActionModel): Action<*>
}