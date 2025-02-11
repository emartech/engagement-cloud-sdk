package com.emarsys.mobileengage.push

interface PushHandler<U : PlatformData, T : SilentPushMessage<U>> {
    suspend fun handle(pushMessage: T)
}