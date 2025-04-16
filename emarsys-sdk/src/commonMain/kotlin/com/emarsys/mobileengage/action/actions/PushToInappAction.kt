package com.emarsys.mobileengage.action.actions

import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.models.PresentablePushToInAppActionModel

class PushToInappAction(
    private val actionModel: PresentablePushToInAppActionModel,
    private val pushToInAppHandler: PushToInAppHandlerApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        pushToInAppHandler.handle(actionModel)
    }
}