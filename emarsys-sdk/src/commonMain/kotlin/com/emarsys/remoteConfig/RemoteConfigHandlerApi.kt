package com.emarsys.remoteConfig

interface RemoteConfigHandlerApi {

    suspend fun handleAppCodeBased()

    suspend fun handleGlobal()

}