package com.sap.ec.networking.clients.remoteConfig

import com.sap.ec.remoteConfig.RemoteConfigResponse

interface RemoteConfigClientApi {

    suspend fun fetchRemoteConfig(global:Boolean=false): RemoteConfigResponse?

}