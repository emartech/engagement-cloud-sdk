package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.sap.ec.mobileengage.inapp.providers.IosWebViewFactoryApi
import com.sap.ec.mobileengage.inapp.view.InAppViewApi
import com.sap.ec.mobileengage.inapp.view.InAppViewProviderApi
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