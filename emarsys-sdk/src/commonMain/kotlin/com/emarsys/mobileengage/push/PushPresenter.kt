package com.emarsys.mobileengage.push

interface PushPresenter {
    suspend fun present(pushMessage: PushMessage)
}