package com.emarsys.core.url

import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.SdkException.MissingApplicationCodeException
import com.emarsys.core.url.EmarsysUrlType.*
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

    override fun create(urlType: EmarsysUrlType): Url {
        return when (urlType) {
            CHANGE_APPLICATION_CODE -> {
                URLBuilder("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client/app").build()
            }

            LINK_CONTACT -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "client/contact"
            ).build()

            UNLINK_CONTACT -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "client/contact"
            ).build()

            REFRESH_TOKEN -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact-token"
            ).build()

            CHANGE_MERCHANT_ID -> createUrl(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact-token"
            ).build()

            PUSH_TOKEN -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client/push-token")
            REGISTER_DEVICE_INFO -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode()}/client")
            EVENT -> {
                Url("${sdkContext.defaultUrls.eventServiceBaseUrl}/$V5_API/apps/${getApplicationCode()}/client/events")
            }

            REMOTE_CONFIG_SIGNATURE -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/${getApplicationCode()}")
            REMOTE_CONFIG -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/${getApplicationCode()}")
            GLOBAL_REMOTE_CONFIG_SIGNATURE -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/GLOBAL")
            GLOBAL_REMOTE_CONFIG -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/GLOBAL")
            DEEP_LINK -> Url(sdkContext.defaultUrls.deepLinkBaseUrl)
            LOGGING -> Url("${sdkContext.defaultUrls.loggingUrl}/v1/log")
            FETCH_EMBEDDED_MESSAGES -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/messages")
            FETCH_BADGE_COUNT -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/badge-count")
            FETCH_META -> Url("${sdkContext.defaultUrls.embeddedMessagingBaseUrl}/$V1_API/${getApplicationCode()}/meta")
        }
    }

    private fun getApplicationCode(): String {
        return sdkContext.config?.applicationCode
            ?: throw MissingApplicationCodeException("Application code is missing!")
    }

    private fun createUrl(
        baseUrl: String,
        mePath: String
    ): URLBuilder {
        return URLBuilder("$baseUrl/$V4_API/apps/${getApplicationCode()}/$mePath")
    }
}
