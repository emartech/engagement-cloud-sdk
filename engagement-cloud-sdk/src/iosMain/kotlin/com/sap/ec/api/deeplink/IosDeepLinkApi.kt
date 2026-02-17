package com.sap.ec.api.deeplink

import platform.Foundation.NSUserActivity

interface IosDeepLinkApi {
    fun track(userActivity: NSUserActivity): Boolean
}