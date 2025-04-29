package com.emarsys.api.deeplink

import com.emarsys.api.deepLink.DeepLinkApi
import com.emarsys.di.SdkKoinIsolationContext.koin
import io.ktor.http.Url
import platform.Foundation.NSUserActivity

class IosDeeplink: IosDeeplinkApi {
    override suspend fun trackDeepLink(userActivity: NSUserActivity) {
        userActivity.webpageURL?.absoluteString()?.let {
            koin.get<DeepLinkApi>().trackDeepLink(Url(it)).getOrThrow()
        }
    }
}