package com.emarsys.mobileengage.inapp.provider

import android.content.Context
import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.InstantProvider
import com.emarsys.mobileengage.inapp.InAppJsBridge
import com.emarsys.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.emarsys.mobileengage.inapp.jsbridge.InAppJsBridgeData
import com.emarsys.mobileengage.inapp.view.InAppView
import com.emarsys.mobileengage.inapp.view.InAppViewApi
import com.emarsys.mobileengage.inapp.view.InAppViewProviderApi
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
