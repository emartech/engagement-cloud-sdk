package com.emarsys.mobileengage.push

interface PushPresenter {
    suspend fun present(push: PushMessage)
}