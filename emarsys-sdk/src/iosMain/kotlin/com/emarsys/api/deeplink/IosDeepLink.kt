package com.emarsys.api.deeplink

import com.emarsys.di.SdkKoinIsolationContext.koin
import io.ktor.http.Url
import platform.Foundation.NSUserActivity

class IosDeepLink: IosDeepLinkApi {
    override suspend fun trackDeepLink(userActivity: NSUserActivity) {
        userActivity.webpageURL?.absoluteString()?.let {
            koin.get<DeepLinkApi>().trackDeepLink(Url(it)).getOrThrow()
        }
    }
}