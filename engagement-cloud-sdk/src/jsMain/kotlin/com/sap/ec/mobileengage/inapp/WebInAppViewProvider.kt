package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.mobileengage.inapp.iframe.IframeFactoryApi
import com.sap.ec.mobileengage.inapp.iframe.MessageChannelProviderApi
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.sap.ec.mobileengage.inapp.view.InAppViewApi
import com.sap.ec.mobileengage.inapp.view.InAppViewProviderApi

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