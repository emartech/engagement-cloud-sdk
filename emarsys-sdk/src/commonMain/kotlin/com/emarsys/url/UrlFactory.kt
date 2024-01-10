package com.emarsys.url

import com.emarsys.context.SdkContextApi
import com.emarsys.core.DefaultUrlsApi
import com.emarsys.url.EmarsysUrlType.REFRESH_TOKEN
import com.emarsys.url.EmarsysUrlType.REGISTER_PUSH_TOKEN

class UrlFactory(
    private val sdkContext: SdkContextApi,
    private val defaultUrls: DefaultUrlsApi
): FactoryApi<EmarsysUrlType, String> {
    private companion object {
        const val V3_API = "v3"
    }

    override fun create(value: EmarsysUrlType): String {
        return when (value) {
           REFRESH_TOKEN -> {
               val baseUrl = defaultUrls.clientServiceBaseUrl
               if (isPredictOnly()) {
                   "$baseUrl/$V3_API/contact-token"
               } else {
                   "$baseUrl/$V3_API/apps/${sdkContext.config?.applicationCode}/contact-token"
               }
           }
            REGISTER_PUSH_TOKEN -> {
                "${defaultUrls.clientServiceBaseUrl}/$V3_API/apps/${sdkContext.config?.applicationCode}/client/push-token"
            }
        }
    }

    private fun isPredictOnly(): Boolean {
        return sdkContext.config?.applicationCode == null && sdkContext.config?.merchantId != null
    }
}
