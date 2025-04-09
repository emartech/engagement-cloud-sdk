package com.emarsys.remoteConfig

interface RemoteConfigResponseHandlerApi {
    suspend fun handle(config: RemoteConfigResponse?)
}