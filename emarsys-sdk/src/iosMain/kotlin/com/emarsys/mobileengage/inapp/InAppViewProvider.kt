package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.InstantProvider
import com.emarsys.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.emarsys.mobileengage.inapp.providers.IosWebViewFactoryApi
import com.emarsys.mobileengage.inapp.view.InAppViewApi
import com.emarsys.mobileengage.inapp.view.InAppViewProviderApi
import kotlinx.coroutines.CoroutineDispatcher

internal class InAppViewProvider(
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: IosWebViewFactoryApi,
    private val timestampProvider: InstantProvider,
    private val contentReplacer: ContentReplacerApi
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        return InAppView(mainDispatcher, webViewProvider, timestampProvider, contentReplacer)
    }
}