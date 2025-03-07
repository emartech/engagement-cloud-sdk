package com.emarsys.mobileengage.push

import com.emarsys.mobileengage.action.models.PresentableActionModel

interface PushPresenter<T: PlatformData, U> where U: PushMessage<T>, U: DisplayablePush, U: ActionablePush<PresentableActionModel> {
    suspend fun present(pushMessage: U)
}
