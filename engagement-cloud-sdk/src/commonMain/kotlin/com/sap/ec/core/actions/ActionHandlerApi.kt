package com.sap.ec.core.actions

import com.sap.ec.mobileengage.action.actions.Action

interface ActionHandlerApi {

    suspend fun handleActions(mandatoryActions: List<Action<*>>, triggeredAction: Action<*>?)
}