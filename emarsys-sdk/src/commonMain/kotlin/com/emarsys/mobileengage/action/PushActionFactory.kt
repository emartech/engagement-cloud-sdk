package com.emarsys.mobileengage.action

import com.emarsys.core.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.LaunchApplicationAction
import com.emarsys.mobileengage.action.actions.PushToInappAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicLaunchApplicationActionModel
import com.emarsys.mobileengage.action.models.BasicPushToInAppActionModel
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel
import com.emarsys.mobileengage.action.models.toInternalPushToInAppActionModel

class PushActionFactory(
    private val pushToInAppHandler: PushToInAppHandlerApi,
    private val eventActionFactory: ActionFactoryApi<ActionModel>,
    private val launchApplicationHandler: LaunchApplicationHandlerApi
) : ActionFactoryApi<ActionModel> {
    override suspend fun create(action: ActionModel): Action<*> {
        return when (action) {
            is InternalPushToInappActionModel -> PushToInappAction(action, pushToInAppHandler)
            is BasicPushToInAppActionModel -> PushToInappAction(
                action.toInternalPushToInAppActionModel(),
                pushToInAppHandler
            )
            is BasicLaunchApplicationActionModel -> LaunchApplicationAction(launchApplicationHandler)
            else -> eventActionFactory.create(action)
        }
    }
}