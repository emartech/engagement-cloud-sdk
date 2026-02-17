package com.sap.ec.mobileengage.inapp.networking.download

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.networking.model.body
import com.sap.ec.mobileengage.inapp.InAppMessage
import com.sap.ec.networking.clients.event.model.ContentCampaign
import com.sap.ec.networking.clients.event.model.asInAppMessage
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal class InAppDownloader(
    private val ecClient: NetworkClientApi,
    private val sdkLogger: Logger
) : InAppDownloaderApi {

    override suspend fun download(url: String): InAppMessage? {
        val request = UrlRequest(Url(url), HttpMethod.Post)

        val contentCampaign: ContentCampaign? = ecClient.send(request).getOrElse {
            currentCoroutineContext().ensureActive()
            sdkLogger.error("Content campaign download failed.", it)
            null
        }?.body()
        return contentCampaign?.asInAppMessage()
    }
}