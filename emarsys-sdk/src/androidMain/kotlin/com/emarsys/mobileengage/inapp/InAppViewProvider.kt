package com.emarsys.mobileengage.inapp

import android.content.Context
import com.emarsys.core.providers.Provider

class InAppViewProvider(private val applicationContext: Context,
                        private val jsBridgeProvider: Provider<InAppJsBridge>
): InAppViewProviderApi {
    override fun provide(): InAppViewApi {
        val inAppView = InAppView(applicationContext)
        inAppView.connectBridge(jsBridgeProvider.provide())
        return inAppView
    }
}
