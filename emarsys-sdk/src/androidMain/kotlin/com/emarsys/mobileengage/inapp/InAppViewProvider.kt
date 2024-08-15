package com.emarsys.mobileengage.inapp

import android.content.Context
import com.emarsys.core.providers.Provider
import kotlinx.coroutines.CoroutineDispatcher

class InAppViewProvider(
    private val applicationContext: Context,
    private val jsBridgeProvider: Provider<InAppJsBridge>,
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: WebViewProvider
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        val inAppView = InAppView(applicationContext, mainDispatcher, webViewProvider)
        inAppView.connectBridge(jsBridgeProvider.provide())
        return inAppView
    }
}
