package com.emarsys.core.actions

import com.emarsys.mobileengage.action.actions.Action

class ActionHandler : ActionHandlerApi {
    override suspend fun handleActions(
        mandatoryActions: List<Action<Unit>>,
        triggeredAction: Action<Unit>
    ) {
        mandatoryActions.forEach { it.invoke() }
        triggeredAction.invoke()
    }
}