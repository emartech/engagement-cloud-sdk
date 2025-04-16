package com.emarsys.core.actions.pushtoinapp

import com.emarsys.mobileengage.action.models.PresentablePushToInAppActionModel

interface PushToInAppHandlerApi {

    suspend fun handle(actionModel: PresentablePushToInAppActionModel)
}