package com.emarsys.mobileengage.session

import com.emarsys.api.SdkResult
import com.emarsys.watchdog.lifecycle.LifecycleWatchDog

interface Session {
    suspend fun subscribe(lifecycleWatchDog: LifecycleWatchDog)
    suspend fun startSession()
    suspend fun endSession()
}