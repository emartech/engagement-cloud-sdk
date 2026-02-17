package com.sap.ec.mobileengage.inapp.provider

import android.content.Context
import com.sap.ec.core.factory.Factory
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.mobileengage.inapp.InAppJsBridge
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.sap.ec.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.sap.ec.mobileengage.inapp.view.InAppView
import com.sap.ec.mobileengage.inapp.view.InAppViewApi
import com.sap.ec.mobileengage.inapp.view.InAppViewProviderApi
import kotlinx.coroutines.CoroutineDispatcher

internal class InAppViewProvider(
    private val applicationContext: Context,
    private val jsBridgeFactory: Factory<InAppJsBridgeData, InAppJsBridge>,
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: WebViewProvider,
    private val timestampProvider: InstantProvider,
    private val contentReplacer: ContentReplacerApi
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        val inAppView = InAppView(
            applicationContext,
            mainDispatcher,
            webViewProvider,
            jsBridgeFactory,
            timestampProvider,
            contentReplacer = contentReplacer
        )
        return inAppView
    }
}
