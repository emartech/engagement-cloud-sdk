package com.emarsys.core.actions

import com.emarsys.mobileengage.action.actions.Action

interface ActionHandlerApi {

    suspend fun handleActions(mandatoryActions: List<Action<*>>, triggeredAction: Action<*>)
}