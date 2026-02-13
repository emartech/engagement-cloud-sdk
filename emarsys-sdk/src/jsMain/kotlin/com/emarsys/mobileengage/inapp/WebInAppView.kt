package com.emarsys.mobileengage.inapp

import com.emarsys.core.providers.InstantProvider
import com.emarsys.mobileengage.inapp.iframe.IframeFactoryApi
import com.emarsys.mobileengage.inapp.iframe.MessageChannelProviderApi
import com.emarsys.mobileengage.inapp.jsbridge.ContentReplacerApi
import com.emarsys.mobileengage.inapp.reporting.InAppLoadingMetric
import com.emarsys.mobileengage.inapp.view.InAppViewApi
import com.emarsys.mobileengage.inapp.webview.WebViewHolder
import web.blob.Blob
import web.blob.BlobPropertyBag
import web.dom.ElementId
import web.events.EventType
import web.events.addEventListener
import web.html.HTMLElement
import web.html.HTMLIFrameElement
import web.messaging.MessageChannel
import web.url.URL
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal class WebInAppView(
    private val timestampProvider: InstantProvider,
    private val contentReplacer: ContentReplacerApi,
    private val iframeFactory: IframeFactoryApi,
    private val messageChannelProvider: MessageChannelProviderApi
) : InAppViewApi {
    private companion object {
        const val TARGET_ORIGIN = "*"
        const val INIT_MESSAGE_CHANNEL = "INIT_MESSAGE_CHANNEL"
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

        val replacedContent = contentReplacer.replace(message.content)
        val iframeContainer = iframeFactory.create()
        val blobUrl = createBlobUrl(replacedContent)
        val messageChannel = messageChannelProvider.provide(message)

        iframeContainer.src = blobUrl
        iframeContainer.id = ElementId(message.dismissId.toIframeId())
        registerOnLoadListener(iframeContainer, messageChannel, blobUrl)

        return WebWebViewHolder(iframeContainer as HTMLElement, inAppLoadingMetric())
    }

    private fun registerOnLoadListener(
        iframeContainer: HTMLIFrameElement,
        messageChannel: MessageChannel,
        blobUrl: String
    ) {
        iframeContainer.addEventListener(EventType("load"), {
            iframeContainer.contentWindow?.postMessage(
                INIT_MESSAGE_CHANNEL,
                TARGET_ORIGIN,
                arrayOf(messageChannel.port2)
            )

            URL.revokeObjectURL(blobUrl)
        })
    }

    private fun createBlobUrl(content: String): String {
        val contentBlob =
            Blob(
                arrayOf(content),
                js("{}").unsafeCast<BlobPropertyBag>().apply {
                    type = "text/html"
                })

        return URL.createObjectURL(contentBlob)
    }
}
