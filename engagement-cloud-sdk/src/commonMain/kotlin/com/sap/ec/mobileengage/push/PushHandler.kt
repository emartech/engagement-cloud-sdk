package com.sap.ec.mobileengage.push

import com.sap.ec.mobileengage.action.models.BasicActionModel

internal interface PushHandler<T: PlatformData, U> where U: PushMessage<T>, U: ActionablePush<BasicActionModel> {
    suspend fun handle(pushMessage: U)
}