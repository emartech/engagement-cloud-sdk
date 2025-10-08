package com.emarsys.mobileengage.action.actions

import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.models.PushToInAppActionModel

class PushToInappAction(
    private val actionModel: PushToInAppActionModel,
    private val pushToInAppHandler: PushToInAppHandlerApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        pushToInAppHandler.handle(actionModel.payload.url)
    }
}