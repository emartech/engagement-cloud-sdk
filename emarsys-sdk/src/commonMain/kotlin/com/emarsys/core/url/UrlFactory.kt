package com.emarsys.core.url

import com.emarsys.context.SdkContextApi
import com.emarsys.context.isConfigPredictOnly
import com.emarsys.core.url.EmarsysUrlType.DEEP_LINK
import com.emarsys.core.url.EmarsysUrlType.EVENT
import com.emarsys.core.url.EmarsysUrlType.GLOBAL_REMOTE_CONFIG
import com.emarsys.core.url.EmarsysUrlType.GLOBAL_REMOTE_CONFIG_SIGNATURE
import com.emarsys.core.url.EmarsysUrlType.LINK_CONTACT
import com.emarsys.core.url.EmarsysUrlType.PUSH_TOKEN
import com.emarsys.core.url.EmarsysUrlType.REFRESH_TOKEN
import com.emarsys.core.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.core.url.EmarsysUrlType.REMOTE_CONFIG
import com.emarsys.core.url.EmarsysUrlType.REMOTE_CONFIG_SIGNATURE
import com.emarsys.core.url.EmarsysUrlType.UNLINK_CONTACT
import io.ktor.http.URLBuilder
import io.ktor.http.Url

class UrlFactory(
    private val sdkContext: SdkContextApi
) : UrlFactoryApi {
    private companion object {
        const val V4_API = "v4"
    }

    override fun create(urlType: EmarsysUrlType): Url {
        return when (urlType) {
            LINK_CONTACT -> createUrlBasedOnPredict(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact-token",
                "client/contact"
            ).build()

            UNLINK_CONTACT -> createUrlBasedOnPredict(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact",
                "client/contact"
            ).build()

            REFRESH_TOKEN -> createUrlBasedOnPredict(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact-token",
                "contact-token"
            ).build()

            PUSH_TOKEN -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${sdkContext.config?.applicationCode}/client/push-token")
            REGISTER_DEVICE_INFO -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${sdkContext.config?.applicationCode}/client")
            EVENT -> {
                Url("${sdkContext.defaultUrls.eventServiceBaseUrl}/$V4_API/apps/${sdkContext.config?.applicationCode}/client/events")
            }

            REMOTE_CONFIG_SIGNATURE -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/${sdkContext.config?.applicationCode}")
            REMOTE_CONFIG -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/${sdkContext.config?.applicationCode}")
            GLOBAL_REMOTE_CONFIG_SIGNATURE -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/GLOBAL")
            GLOBAL_REMOTE_CONFIG -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/GLOBAL")
            DEEP_LINK -> Url(sdkContext.defaultUrls.deepLinkBaseUrl)
        }
    }

    private fun createUrlBasedOnPredict(
        baseUrl: String,
        predictPath: String,
        mePath: String
    ): URLBuilder {
        return if (sdkContext.isConfigPredictOnly()) {
            URLBuilder("$baseUrl/$V4_API/$predictPath")
        } else {
            URLBuilder("$baseUrl/$V4_API/apps/${sdkContext.config?.applicationCode}/$mePath")
        }
    }
}
