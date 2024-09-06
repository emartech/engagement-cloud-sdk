package com.emarsys.core.pushtoinapp

import com.emarsys.mobileengage.action.models.InternalPushToInappActionModel

interface PushToInAppHandlerApi {

    suspend fun handle(actionModel: InternalPushToInappActionModel)
}