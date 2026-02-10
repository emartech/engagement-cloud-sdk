package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.InstantProvider
import com.emarsys.mobileengage.inapp.iframe.ContentReplacerApi
import com.emarsys.mobileengage.inapp.iframe.IframeFactoryApi
import com.emarsys.mobileengage.inapp.iframe.MessageChannelProviderApi

internal class WebInAppViewProvider(
    private val timestampProvider: InstantProvider,
    private val contentReplacer: ContentReplacerApi,
    private val iframeFactory: IframeFactoryApi,
    private val messageChannelProvider: MessageChannelProviderApi,
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        return WebInAppView(
            timestampProvider,
            contentReplacer,
            iframeFactory,
            messageChannelProvider
        )
    }
}