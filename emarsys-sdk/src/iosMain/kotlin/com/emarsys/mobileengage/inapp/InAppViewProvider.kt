package com.emarsys.mobileengage.inapp

import com.emarsys.core.factory.SuspendFactory
import com.emarsys.core.providers.InstantProvider
import kotlinx.coroutines.CoroutineDispatcher
import platform.WebKit.WKWebView

internal class InAppViewProvider(
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: SuspendFactory<String, WKWebView>,
    private val timestampProvider: InstantProvider
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        return InAppView(mainDispatcher, webViewProvider, timestampProvider)
    }
}