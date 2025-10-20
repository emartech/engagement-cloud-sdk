package com.emarsys.api.deeplink

import com.emarsys.di.SdkKoinIsolationContext.koin
import io.ktor.http.Url
import platform.Foundation.NSUserActivity

class IosDeepLink: IosDeepLinkApi {
    override fun track(userActivity: NSUserActivity): Boolean {
        return userActivity.webpageURL?.absoluteString()?.let {
            koin.get<DeepLinkApi>().track(Url(it)).getOrNull()
        } ?: false
    }
}