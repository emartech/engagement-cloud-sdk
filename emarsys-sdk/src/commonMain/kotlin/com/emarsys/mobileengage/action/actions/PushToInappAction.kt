package com.emarsys.mobileengage.action.actions

import com.emarsys.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.emarsys.mobileengage.action.models.PushToInappActionModel

class PushToInappAction(
    private val actionModel: PushToInappActionModel,
    private val pushToInAppHandler: PushToInAppHandlerApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        pushToInAppHandler.handle(actionModel.payload.url)
    }
}