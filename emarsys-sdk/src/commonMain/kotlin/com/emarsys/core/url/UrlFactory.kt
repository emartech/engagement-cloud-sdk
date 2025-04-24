package com.emarsys.core.url

import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.MissingApplicationCodeException
import com.emarsys.core.url.EmarsysUrlType.CHANGE_APPLICATION_CODE
import com.emarsys.core.url.EmarsysUrlType.CHANGE_MERCHANT_ID
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

    override fun create(urlType: EmarsysUrlType, applicationCode: String?): Url {
        return when (urlType) {
            CHANGE_APPLICATION_CODE -> {
                URLBuilder("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode(applicationCode)}/client/app").build()
            }
            LINK_CONTACT -> createUrlBasedOnPredict(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact-token",
                "client/contact",
                applicationCode
            ).build()

            UNLINK_CONTACT -> createUrlBasedOnPredict(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact",
                "client/contact",
                applicationCode
            ).build()

            REFRESH_TOKEN -> createUrlBasedOnPredict(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact-token",
                "contact-token",
                applicationCode
            ).build()

            CHANGE_MERCHANT_ID -> createUrlBasedOnPredict(
                sdkContext.defaultUrls.clientServiceBaseUrl,
                "contact-token",
                "contact-token",
                applicationCode
            ).build()

            PUSH_TOKEN -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode(applicationCode)}/client/push-token")
            REGISTER_DEVICE_INFO -> Url("${sdkContext.defaultUrls.clientServiceBaseUrl}/$V4_API/apps/${getApplicationCode(applicationCode)}/client")
            EVENT -> {
                Url("${sdkContext.defaultUrls.eventServiceBaseUrl}/$V4_API/apps/${getApplicationCode(applicationCode)}/client/events")
            }

            REMOTE_CONFIG_SIGNATURE -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/${getApplicationCode(applicationCode)}")
            REMOTE_CONFIG -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/${getApplicationCode(applicationCode)}")
            GLOBAL_REMOTE_CONFIG_SIGNATURE -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/signature/GLOBAL")
            GLOBAL_REMOTE_CONFIG -> Url("${sdkContext.defaultUrls.remoteConfigBaseUrl}/GLOBAL")
            DEEP_LINK -> Url(sdkContext.defaultUrls.deepLinkBaseUrl)
            EmarsysUrlType.LOGGING -> Url("${sdkContext.defaultUrls.loggingUrl}/v1/log")
        }
    }

    private fun getApplicationCode(applicationCode: String?): String {
        val appCode = applicationCode ?: sdkContext.config?.applicationCode
        return appCode ?: throw MissingApplicationCodeException("Application code is missing!")
    }

    private fun createUrlBasedOnPredict(
        baseUrl: String,
        predictPath: String,
        mePath: String,
        appCode: String? = null
    ): URLBuilder {
        return if (sdkContext.isConfigPredictOnly()) {
            URLBuilder("$baseUrl/$V4_API/$predictPath")
        } else {
            URLBuilder("$baseUrl/$V4_API/apps/${getApplicationCode(appCode)}/$mePath")
        }
    }
}
