package com.emarsys.mobileengage.inapp

import android.content.Context
import com.emarsys.core.factory.Factory
import kotlinx.coroutines.CoroutineDispatcher

class InAppViewProvider(
    private val applicationContext: Context,
    private val jsBridgeProvider: Factory<String,InAppJsBridge>,
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: WebViewProvider
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        val inAppView = InAppView(applicationContext, mainDispatcher, webViewProvider, jsBridgeProvider)
        return inAppView
    }
}
