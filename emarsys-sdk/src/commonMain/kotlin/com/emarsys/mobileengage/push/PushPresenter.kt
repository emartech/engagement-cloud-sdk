package com.emarsys.mobileengage.push

interface PushPresenter<U : PlatformData, T : PresentablePushMessage<U>> {
    suspend fun present(pushMessage: T)
}
