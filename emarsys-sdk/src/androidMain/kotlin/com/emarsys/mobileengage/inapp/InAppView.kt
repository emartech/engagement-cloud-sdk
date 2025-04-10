package com.emarsys.mobileengage.inapp

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.emarsys.core.factory.Factory
import com.emarsys.core.providers.InstantProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
internal class InAppView @JvmOverloads constructor(
    context: Context,
    private val mainDispatcher: CoroutineDispatcher,
    private val webViewProvider: WebViewProvider,
    private val jsBridgeProvider: Factory<String, InAppJsBridge>,
    private val timestampProvider: InstantProvider,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), InAppViewApi {

    companion object {
        const val JS_BRIDGE_NAME = "Android"
    }

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

        return withContext(mainDispatcher) {
            val webView = webViewProvider.provide()
            webView.loadDataWithBaseURL(null, message.html, "text/html", "UTF-8", null)

            addView(webView)
            webView.addJavascriptInterface(
                jsBridgeProvider.create(message.campaignId),
                JS_BRIDGE_NAME
            )

            AndroidWebViewHolder(webView, inAppLoadingMetric())
        }
    }
}