package com.sap.ec.core.url

import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.exceptions.SdkException.MissingApplicationCodeException
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

    override suspend fun create(urlType: ECUrlType): Url {
        return when (urlType) {
            ECUrlType.RemoteConfigSignature -> Url("${sdkContext.serviceUrls.remoteConfigBaseUrl}/signature/${getApplicationCode()}")
            ECUrlType.RemoteConfig -> Url("${sdkContext.serviceUrls.remoteConfigBaseUrl}/${getApplicationCode()}")
            ECUrlType.GlobalRemoteConfigSignature -> Url("${sdkContext.serviceUrls.remoteConfigBaseUrl}/signature/GLOBAL")
            ECUrlType.GlobalRemoteConfig -> Url("${sdkContext.serviceUrls.remoteConfigBaseUrl}/GLOBAL")

            ECUrlType.DeepLink -> {
                sdkContext.serviceUrls.globalFeatureUrls?.let {
                    Url(it.deepLinkBaseUrl)
                } ?: throw IllegalStateException("Service URLs are not available")
            }

            else -> {
                sdkContext.serviceUrls.featureUrls?.let {
                    when (urlType) {
                        ECUrlType.ChangeApplicationCode -> {
                            URLBuilder("${it.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client/app").build()
                        }

                        ECUrlType.LinkContact -> createUrl(
                            it.clientServiceBaseUrl,
                            "client/contact"
                        ).build()

                        is ECUrlType.UnlinkContact ->
                            Url("${it.clientServiceBaseUrl}/$V4_API/apps/${urlType.applicationCode}/client/contact")

                        ECUrlType.RefreshToken -> createUrl(
                            it.clientServiceBaseUrl,
                            "client/contact-token"
                        ).build()

                        ECUrlType.PushToken -> Url("${it.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client/push-token")
                        is ECUrlType.ClearPushToken ->
                            Url("${it.clientServiceBaseUrl}/$V4_API/apps/${urlType.applicationCode}/client/push-token")

                        ECUrlType.RegisterDeviceInfo -> Url("${it.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client")
                        ECUrlType.Event -> {
                            Url("${it.eventServiceBaseUrl}/$V5_API/apps/${getApplicationCode()}/client/events")
                        }

                        ECUrlType.FetchEmbeddedMessages -> Url("${it.embeddedMessagingBaseUrl}/embedded-messaging/api/$V1_API/${getApplicationCode()}/messages")
                        ECUrlType.FetchBadgeCount -> Url("${it.embeddedMessagingBaseUrl}/embedded-messaging/api/$V1_API/${getApplicationCode()}/badge-count")
                        ECUrlType.FetchMeta -> Url("${it.embeddedMessagingBaseUrl}/embedded-messaging/api/$V1_API/${getApplicationCode()}/meta")
                        ECUrlType.UpdateTagsForMessages -> Url("${it.embeddedMessagingBaseUrl}/embedded-messaging/api/$V1_API/${getApplicationCode()}/tags")
                        is ECUrlType.FetchInlineInAppMessages -> {
                            Url("${it.eventServiceBaseUrl}/$V5_API/apps/${getApplicationCode()}/inline-messages")
                        }

                        ECUrlType.Logging -> Url("${it.loggingUrl}/v1/log")
                    }
                } ?: throw IllegalStateException("Service URLs are not available")
            }
        }
    }

    private suspend fun getApplicationCode(): String {
        return sdkContext.getSdkConfig()?.applicationCode
            ?: throw MissingApplicationCodeException("Application code is missing!")
    }

    private suspend fun createUrl(
        baseUrl: String,
        mePath: String
    ): URLBuilder {
        return URLBuilder("$baseUrl/$V4_API/apps/${getApplicationCode()}/$mePath")
    }
}
