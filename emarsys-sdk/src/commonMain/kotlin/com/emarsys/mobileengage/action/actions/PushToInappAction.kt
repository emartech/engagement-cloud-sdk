package com.emarsys.mobileengage.action.actions

import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel

class PushToInappAction(
    private val actionModel: InternalPushToInappActionModel,
    private val pushToInAppHandler: PushToInAppHandlerApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        pushToInAppHandler.handle(actionModel)
    }
}