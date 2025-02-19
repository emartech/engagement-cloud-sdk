package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.SuspendFactory
import kotlinx.coroutines.CoroutineDispatcher
import platform.WebKit.WKWebView

class InAppViewProvider(
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: SuspendFactory<String,WKWebView>
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        return InAppView(mainDispatcher, webViewProvider)
    }
}