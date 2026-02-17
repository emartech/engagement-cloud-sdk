package com.sap.ec.remoteConfig

interface RemoteConfigResponseHandlerApi {
    suspend fun handle(config: RemoteConfigResponse?)
}