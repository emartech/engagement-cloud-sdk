package com.emarsys.url

import com.emarsys.context.SdkContextApi
import com.emarsys.core.DefaultUrlsApi
import com.emarsys.url.EmarsysUrlType.LINK_CONTACT
import com.emarsys.url.EmarsysUrlType.REFRESH_TOKEN
import com.emarsys.url.EmarsysUrlType.REGISTER_DEVICE_INFO
import com.emarsys.url.EmarsysUrlType.REGISTER_PUSH_TOKEN

class UrlFactory(
    private val sdkContext: SdkContextApi,
    private val defaultUrls: DefaultUrlsApi
) : FactoryApi<EmarsysUrlType, String> {
    private companion object {
        const val V3_API = "v3"
    }

    override fun create(value: EmarsysUrlType): String {
        return when (value) {
            LINK_CONTACT -> createUrlBasedOnPredict(
                defaultUrls.clientServiceBaseUrl,
                "contact",
                "client/contact"
            )

            REFRESH_TOKEN -> createUrlBasedOnPredict(
                defaultUrls.clientServiceBaseUrl,
                "contact-token",
                "contact-token"
            )

            REGISTER_PUSH_TOKEN -> "${defaultUrls.clientServiceBaseUrl}/$V3_API/apps/${sdkContext.config?.applicationCode}/client/push-token"
            REGISTER_DEVICE_INFO -> "${defaultUrls.clientServiceBaseUrl}/$V3_API/apps/${sdkContext.config?.applicationCode}/client"
        }
    }

    private fun createUrlBasedOnPredict(
        baseUrl: String,
        predictPath: String,
        mePath: String
    ): String {
        return if (isPredictOnly()) {
            "$baseUrl/$V3_API/$predictPath"
        } else {
            "$baseUrl/$V3_API/apps/${sdkContext.config?.applicationCode}/$mePath"
        }
    }

    private fun isPredictOnly(): Boolean {
        return sdkContext.config?.applicationCode == null && sdkContext.config?.merchantId != null
    }
}
