package com.sap.ec.mobileengage.session

import com.sap.ec.watchdog.lifecycle.LifecycleWatchDog

interface SessionApi {
    suspend fun subscribe(lifecycleWatchDog: LifecycleWatchDog)
    suspend fun startSession()
    suspend fun endSession()
}