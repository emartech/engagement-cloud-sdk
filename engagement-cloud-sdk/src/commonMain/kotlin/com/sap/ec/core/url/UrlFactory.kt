package com.sap.ec.core.url

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException.MissingApplicationCodeException
import com.sap.ec.event.OnlineSdkEvent
import com.sap.ec.event.SdkEvent
import io.ktor.http.URLBuilder
import io.ktor.http.Url

internal class UrlFactory(
    private val sdkContext: SdkContextApi
) : UrlFactoryApi {
    private companion object {
        const val V1_API = "v1"
        const val V4_API = "v4"
        const val V5_API = "v5"
    }

    //TODO: remove sdkEvent, add to the URLType if needed
    override fun create(urlType: ECUrlType, sdkEvent: OnlineSdkEvent?): Url {
        return when (urlType) {
            ECUrlType.ChangeApplicationCode -> {
                URLBuilder("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client/app").build()
            }

            ECUrlType.LinkContact -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "client/contact"
            ).build()

            ECUrlType.UnlinkContact -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "client/contact"
            ).build()

            ECUrlType.RefreshToken -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "client/contact-token"
            ).build()

            ECUrlType.ChangeMerchantId -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "client/contact-token"
            ).build()

            ECUrlType.PushToken -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client/push-token")
            ECUrlType.ClearPushToken ->
                Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCodeFromEvent(sdkEvent)}/client/push-token")

            ECUrlType.RegisterDeviceInfo -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client")
            ECUrlType.Event -> {
                Url("${sdkContext.defaultUrls.eventServiceBaseUrl}/$V5_API/apps/${getApplicationCode()}/client/events")
            }

            ECUrlType.RemoteConfigSignature -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/${getApplicationCode()}")
            ECUrlType.RemoteConfig -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/${getApplicationCode()}")
            ECUrlType.GlobalRemoteConfigSignature -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/GLOBAL")
            ECUrlType.GlobalRemoteConfig -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/GLOBAL")
            ECUrlType.DeepLink -> Url(sdkContext.defaultUrls.deepLinkBaseUrl)
            ECUrlType.Logging -> Url("${sdkContext.defaultUrls.loggingUrl}/v1/log")
            ECUrlType.FetchEmbeddedMessages -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/messages")
            ECUrlType.FetchBadgeCount -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/badge-count")
            ECUrlType.FetchMeta -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/meta")
            ECUrlType.UpdateTagsForMessages -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/tags")
            is ECUrlType.FetchInlineInAppMessages -> {
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
