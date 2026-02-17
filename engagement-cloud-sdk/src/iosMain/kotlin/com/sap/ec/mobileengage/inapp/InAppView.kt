package com.sap.ec.mobileengage.inapp

import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.sap.ec.mobileengage.inapp.providers.IosWebViewFactoryApi
import com.sap.ec.mobileengage.inapp.reporting.InAppLoadingMetric
import com.sap.ec.mobileengage.inapp.view.InAppViewApi
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class InAppView(
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewFactory: IosWebViewFactoryApi,
    private val timestampProvider: InstantProvider,
    private val contentReplacer: ContentReplacerApi
) : InAppViewApi {
    private lateinit var mInAppMessage: InAppMessage
    private var loadingStarted: Long? = null
    override val inAppMessage: InAppMessage
        get() = mInAppMessage

    private fun inAppLoadingMetric(): InAppLoadingMetric {
        return InAppLoadingMetric(
            loadingStarted = loadingStarted ?: 0,
            loadingEnded = timestampProvider.provide().toEpochMilliseconds()
        )
    }

    override suspend fun load(message: InAppMessage): WebViewHolder {
        loadingStarted = timestampProvider.provide().toEpochMilliseconds()

        mInAppMessage = message
        val replacedContent = contentReplacer.replace(message.content)
        val webView = withContext(mainDispatcher) {
            webViewFactory.create(message.dismissId, message.trackingInfo).apply {
                loadHTMLString(replacedContent, null)
            }
        }
        return IosWebViewHolder(webView, inAppLoadingMetric())
    }
}
