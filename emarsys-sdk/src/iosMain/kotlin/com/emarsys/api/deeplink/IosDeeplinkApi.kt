package com.emarsys.api.deeplink

import platform.Foundation.NSUserActivity
import kotlin.coroutines.cancellation.CancellationException

interface IosDeeplinkApi {
    @Throws(CancellationException::class)
    suspend fun trackDeepLink(userActivity: NSUserActivity)
}