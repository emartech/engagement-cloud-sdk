package com.sap.ec.remoteConfig

internal interface RemoteConfigResponseHandlerApi {
    suspend fun handle(config: RemoteConfigResponse?)
}