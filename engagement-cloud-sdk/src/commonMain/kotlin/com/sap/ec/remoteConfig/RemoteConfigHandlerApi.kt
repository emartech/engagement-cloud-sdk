package com.sap.ec.remoteConfig

internal interface RemoteConfigHandlerApi {
    suspend fun handle(remoteConfig: RemoteConfig)
}