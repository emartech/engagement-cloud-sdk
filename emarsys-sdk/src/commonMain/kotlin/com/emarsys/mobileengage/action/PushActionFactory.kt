package com.emarsys.mobileengage.action

import com.emarsys.core.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.PushToInappAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel

class PushActionFactory(
    private val pushToInAppHandler: PushToInAppHandlerApi,
    private val eventActionFactory: ActionFactoryApi<ActionModel>
): ActionFactoryApi<ActionModel> {
    override suspend fun create(action: ActionModel): Action<*> {
        return when (action) {
            is InternalPushToInappActionModel -> PushToInappAction(action, pushToInAppHandler)
            else -> eventActionFactory.create(action)
        }
    }
}