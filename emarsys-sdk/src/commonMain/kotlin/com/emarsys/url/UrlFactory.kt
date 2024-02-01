package com.emarsys.url

import com.emarsys.context.SdkContextApi
import com.emarsys.url.EmarsysUrlType.EVENT
import com.emarsys.url.EmarsysUrlType.LINK_CONTACT
import com.emarsys.url.EmarsysUrlType.REFRESH_TOKEN
import com.emarsys.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.url.EmarsysUrlType.REGISTER_PUSH_TOKEN
import com.emarsys.url.EmarsysUrlType.*
import io.ktor.http.*

class UrlFactory(
    private val sdkContext: SdkContextApi
) : UrlFactoryApi {
    private companion object {
        const val V3_API = "v3"
        const val V4_API = "v4"
    }

    override fun create(urlType: EmarsysUrlType): Url {
        return when (urlType) {
            LINK_CONTACT -> createUrlBasedOnPredict(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact",
                "client/contact"
            ).build()

            UNLINK_CONTACT -> createUrlBasedOnPredict(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact",
                "client/contact"
            ).apply { parameters.append("anonymous", "true") }.build()

            REFRESH_TOKEN -> createUrlBasedOnPredict(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact-token",
                "contact-token"
            ).build()

            REGISTER_PUSH_TOKEN -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V3_API/apps/${sdkContext.config?.applicationCode}/client/push-token")
            REGISTER_DEVICE_INFO -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V3_API/apps/${sdkContext.config?.applicationCode}/client")
            EVENT -> {
                Url("${sdkContext.defaultUrls.eventServiceBaseUrl}/$V4_API/apps/${sdkContext.config?.applicationCode}/client/events")
            }

            REMOTE_CONFIG_SIGNATURE -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/${sdkContext.config?.applicationCode}")
            REMOTE_CONFIG -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/${sdkContext.config?.applicationCode}")
        }
    }

    private fun createUrlBasedOnPredict(
        baseUrl: String,
        predictPath: String,
        mePath: String
    ): URLBuilder {
        return if (isPredictOnly()) {
            URLBuilder("$baseUrl/$V3_API/$predictPath")
        } else {
            URLBuilder("$baseUrl/$V3_API/apps/${sdkContext.config?.applicationCode}/$mePath")
        }
    }

    private fun isPredictOnly(): Boolean {
        return sdkContext.config?.applicationCode == null && sdkContext.config?.merchantId != null
    }
}
