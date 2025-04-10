package com.emarsys.mobileengage.inapp

import android.content.Context
import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.InstantProvider
import kotlinx.coroutines.CoroutineDispatcher

internal class InAppViewProvider(
    private val applicationContext: Context,
    private val jsBridgeProvider: Factory<String, InAppJsBridge>,
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: WebViewProvider,
    private val timestampProvider: InstantProvider,
) : InAppViewProviderApi {
    override suspend fun provide(): InAppViewApi {
        val inAppView = InAppView(
            applicationContext,
            mainDispatcher,
            webViewProvider,
            jsBridgeProvider,
            timestampProvider
        )
        return inAppView
    }
}
