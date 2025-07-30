package com.emarsys.mobileengage.inapp

import com.emarsys.core.log.Logger
import com.emarsys.core.networking.clients.NetworkClientApi
import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.core.networking.model.body
import com.emarsys.networking.clients.event.model.ContentCampaign
import com.emarsys.networking.clients.event.model.asInAppMessage
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

internal class InAppDownloader(
    private val emarsysClient: NetworkClientApi,
    private val sdkLogger: Logger
) : InAppDownloaderApi {

    override suspend fun download(url: String): InAppMessage? {
        val request = UrlRequest(Url(url), HttpMethod.Post)

        val contentCampaign: ContentCampaign? = try {
            emarsysClient.send(request).body()
        } catch (exception: Exception) {
            coroutineContext.ensureActive()
            sdkLogger.error("Content campaign download failed.", exception)
            null
        }

        return contentCampaign?.asInAppMessage()
    }
}