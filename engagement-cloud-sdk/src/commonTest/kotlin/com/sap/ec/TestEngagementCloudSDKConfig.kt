package com.sap.ec

import com.sap.ec.config.SdkConfig
import kotlinx.serialization.Serializable

@Serializable
data class TestEngagementCloudSDKConfig(
    override val applicationCode: String,
) : SdkConfig {
    override fun copyWith(
        applicationCode: String
    ): SdkConfig {
        return copy(
            applicationCode = applicationCode
        )
    }
}