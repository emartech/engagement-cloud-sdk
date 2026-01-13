package com.emarsys.core.url

import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.SdkException.MissingApplicationCodeException
import com.emarsys.event.OnlineSdkEvent
import com.emarsys.event.SdkEvent
import io.ktor.http.URLBuilder
import io.ktor.http.Url

internal class UrlFactory(
    private val sdkContext: SdkContextApi
) : UrlFactoryApi {
    private companion object {
        const val V4_API = "v4"

        const val V1_API = "v1"
        const val V5_API = "v5"
    }

    override fun create(urlType: EmarsysUrlType, sdkEvent: OnlineSdkEvent?): Url {
        return when (urlType) {
            EmarsysUrlType.ChangeApplicationCode -> {
                URLBuilder("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client/app").build()
            }

            EmarsysUrlType.LinkContact -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "client/contact"
            ).build()

            EmarsysUrlType.UnlinkContact -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "client/contact"
            ).build()

            EmarsysUrlType.RefreshToken -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "client/contact-token"
            ).build()

            EmarsysUrlType.ChangeMerchantId -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "client/contact-token"
            ).build()

            EmarsysUrlType.PushToken -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client/push-token")
            EmarsysUrlType.ClearPushToken ->
                Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCodeFromEvent(sdkEvent)}/client/push-token")

            EmarsysUrlType.RegisterDeviceInfo -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client")
            EmarsysUrlType.Event -> {
                Url("${sdkContext.defaultUrls.eventServiceBaseUrl}/$V5_API/apps/${getApplicationCode()}/client/events")
            }

            EmarsysUrlType.RemoteConfigSignature -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/${getApplicationCode()}")
            EmarsysUrlType.RemoteConfig -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/${getApplicationCode()}")
            EmarsysUrlType.GlobalRemoteConfigSignature -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/GLOBAL")
            EmarsysUrlType.GlobalRemoteConfig -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/GLOBAL")
            EmarsysUrlType.DeepLink -> Url(sdkContext.defaultUrls.deepLinkBaseUrl)
            EmarsysUrlType.Logging -> Url("${sdkContext.defaultUrls.loggingUrl}/v1/log")
            EmarsysUrlType.FetchEmbeddedMessages -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/messages")
            EmarsysUrlType.FetchBadgeCount -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/badge-count")
            EmarsysUrlType.FetchMeta -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/meta")
            EmarsysUrlType.UpdateTagsForMessages -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/tags")
            is EmarsysUrlType.FetchInlineInAppMessages -> {
                Url("${sdkContext.defaultUrls.eventServiceBaseUrl}/$V5_API/apps/${getApplicationCode()}/inline-messages")
            }
        }
    }

    private fun getApplicationCode(): String {
        return sdkContext.config?.applicationCode
            ?: throw MissingApplicationCodeException("Application code is missing!")
    }

    private fun getApplicationCodeFromEvent(sdkEvent: OnlineSdkEvent?): String {
        return (sdkEvent as? SdkEvent.Internal.OperationalEvent)?.applicationCode
            ?: throw MissingApplicationCodeException("Application code is missing!")
    }

    private fun createUrl(
        baseUrl: String,
        mePath: String
    ): URLBuilder {
        return URLBuilder("$baseUrl/$V4_API/apps/${getApplicationCode()}/$mePath")
    }
}
