package com.emarsys.networking.clients.remoteConfig

import com.emarsys.remoteConfig.RemoteConfigResponse

interface RemoteConfigClientApi {

    suspend fun fetchRemoteConfig(global:Boolean=false): RemoteConfigResponse?

}