package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.BasicActionModel

interface PushHandler<T: PlatformData, U> where U: PushMessage<T>, U: ActionablePush<BasicActionModel> {
    suspend fun handle(pushMessage: U)
}