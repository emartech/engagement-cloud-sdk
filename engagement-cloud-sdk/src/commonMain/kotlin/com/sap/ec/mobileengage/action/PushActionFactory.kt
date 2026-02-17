package com.sap.ec.mobileengage.action

import com.sap.ec.core.actions.launchapplication.LaunchApplicationHandlerApi
import com.sap.ec.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.sap.ec.mobileengage.action.actions.Action
import com.sap.ec.mobileengage.action.actions.LaunchApplicationAction
import com.sap.ec.mobileengage.action.actions.PushToInappAction
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.action.models.BasicLaunchApplicationActionModel
import com.sap.ec.mobileengage.action.models.BasicPushToInAppActionModel
import com.sap.ec.mobileengage.action.models.PresentablePushToInAppActionModel

internal class PushActionFactory(
    private val pushToInAppHandler: PushToInAppHandlerApi,
    private val eventActionFactory: EventActionFactoryApi,
    private val launchApplicationHandler: LaunchApplicationHandlerApi
) : PushActionFactoryApi {
    override suspend fun create(actionModel: ActionModel): Action<*> {
        return when (actionModel) {
            is BasicPushToInAppActionModel,
            is PresentablePushToInAppActionModel -> PushToInappAction(actionModel, pushToInAppHandler)
            is BasicLaunchApplicationActionModel -> LaunchApplicationAction(launchApplicationHandler)
            else -> eventActionFactory.create(actionModel)
        }
    }
}