package com.sap.ec.api.deeplink

import com.sap.ec.di.SdkKoinIsolationContext.koin
import io.ktor.http.Url
import platform.Foundation.NSUserActivity

class IosDeepLink: IosDeepLinkApi {
    override fun track(userActivity: NSUserActivity): Boolean {
        return userActivity.webpageURL?.absoluteString()?.let {
            koin.get<DeepLinkApi>().track(Url(it)).getOrNull()
        } ?: false
    }
}