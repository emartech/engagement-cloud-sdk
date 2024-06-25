package com.emarsys.mobileengage.push

interface PushPresenter<U : PlatformData, T : PushMessage<U>> {
    suspend fun present(pushMessage: T)
}