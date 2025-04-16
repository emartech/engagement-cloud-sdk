package com.emarsys.mobileengage.action

import com.emarsys.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.actions.Action
import com.emarsys.mobileengage.action.actions.LaunchApplicationAction
import com.emarsys.mobileengage.action.actions.PushToInappAction
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.action.models.BasicLaunchApplicationActionModel
import com.emarsys.mobileengage.action.models.BasicPushToInAppActionModel
import com.emarsys.mobileengage.action.models.PresentablePushToInAppActionModel
import com.emarsys.mobileengage.action.models.toPresentablePushToInAppActionModel

internal class PushActionFactory(
    private val pushToInAppHandler: PushToInAppHandlerApi,
    private val eventActionFactory: EventActionFactoryApi,
    private val launchApplicationHandler: LaunchApplicationHandlerApi
) : PushActionFactoryApi {
    override suspend fun create(action: ActionModel): Action<*> {
        return when (action) {
            is BasicPushToInAppActionModel -> PushToInappAction(action.toPresentablePushToInAppActionModel(), pushToInAppHandler)
            is PresentablePushToInAppActionModel -> PushToInappAction(action, pushToInAppHandler)
            is BasicLaunchApplicationActionModel -> LaunchApplicationAction(launchApplicationHandler)
            else -> eventActionFactory.create(action)
        }
    }
}