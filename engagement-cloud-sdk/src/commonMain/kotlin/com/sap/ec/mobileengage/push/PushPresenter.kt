package com.sap.ec.mobileengage.push

import com.sap.ec.InternalSdkApi
import com.sap.ec.mobileengage.action.models.PresentableActionModel

internal interface PushPresenter<T: PlatformData, U> where U: PushMessage<T>, U: DisplayablePush, U: ActionablePush<PresentableActionModel> {
    suspend fun present(pushMessage: U)
}
