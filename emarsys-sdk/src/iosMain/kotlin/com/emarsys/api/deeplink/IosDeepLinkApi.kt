package com.emarsys.api.deeplink

import platform.Foundation.NSUserActivity
import kotlin.coroutines.cancellation.CancellationException

interface IosDeepLinkApi {
    @Throws(CancellationException::class)
    suspend fun track(userActivity: NSUserActivity)
}