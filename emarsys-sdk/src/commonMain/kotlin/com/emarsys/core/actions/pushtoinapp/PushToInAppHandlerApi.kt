package com.emarsys.core.actions.pushtoinapp

import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel

interface PushToInAppHandlerApi {

    suspend fun handle(actionModel: InternalPushToInappActionModel)
}