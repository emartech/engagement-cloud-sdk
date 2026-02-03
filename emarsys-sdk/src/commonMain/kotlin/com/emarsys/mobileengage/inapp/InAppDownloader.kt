package com.emarsys.mobileengage.inapp

import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.networking.clients.event.model.ContentCampaign
import com.emarsys.networking.clients.event.model.asInAppMessage
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal class InAppDownloader(
    private val emarsysClient: NetworkClientApi,
    private val sdkLogger: Logger
) : InAppDownloaderApi {

    override suspend fun download(url: String): InAppMessage? {
        val request = UrlRequest(Url(url), HttpMethod.Post)

        val contentCampaign: ContentCampaign? = emarsysClient.send(request).getOrElse {
            currentCoroutineContext().ensureActive()
            sdkLogger.error("Content campaign download failed.", it)
            null
        }?.body()
        return contentCampaign?.asInAppMessage()
    }
}