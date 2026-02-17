package com.sap.ec.mobileengage.action.actions

import com.sap.ec.core.actions.pushtoinapp.PushToInAppHandlerApi
import com.sap.ec.mobileengage.action.models.PushToInAppActionModel

class PushToInappAction(
    private val actionModel: PushToInAppActionModel,
    private val pushToInAppHandler: PushToInAppHandlerApi
) : Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        pushToInAppHandler.handle(actionModel.payload.url)
    }
}