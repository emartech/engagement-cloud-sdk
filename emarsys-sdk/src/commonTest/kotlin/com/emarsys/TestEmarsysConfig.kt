package com.emarsys

import com.emarsys.config.SdkConfig
import kotlinx.serialization.Serializable

@Serializable
data class TestEmarsysConfig(
    override val applicationCode: String? = null,
    override val merchantId: String? = null,
    override val sharedSecret: String? = null
) : SdkConfig {
    override fun copyWith(
        applicationCode: String?,
        merchantId: String?,
        sharedSecret: String?
    ): SdkConfig {
        return copy(
            applicationCode = applicationCode,
            merchantId = merchantId,
            sharedSecret = sharedSecret
        )
    }
}