package com.emarsys.api.deeplink

import platform.Foundation.NSUserActivity

interface IosDeepLinkApi {
    fun track(userActivity: NSUserActivity): Boolean
}