package com.sap.ec.mobileengage.action

import com.sap.ec.mobileengage.action.actions.Action

interface ActionFactoryApi<ActionModelType> {
    suspend fun create(actionModel: ActionModelType): Action<*>
}
