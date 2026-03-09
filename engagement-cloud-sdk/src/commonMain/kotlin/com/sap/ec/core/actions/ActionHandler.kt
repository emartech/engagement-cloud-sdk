package com.sap.ec.core.actions

import com.sap.ec.mobileengage.action.actions.Action

internal class ActionHandler : ActionHandlerApi {
    override suspend fun handleActions(
        mandatoryActions: List<Action<*>>,
        triggeredAction: Action<*>?
    ) {
        mandatoryActions.forEach { it.invoke() }
        triggeredAction?.invoke()
    }
}