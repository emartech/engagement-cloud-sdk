package com.emarsys

import com.emarsys.config.SdkConfig
import kotlinx.serialization.Serializable

@Serializable
data class TestEmarsysConfig(
    override val applicationCode: String? = null
) : SdkConfig {
    override fun copyWith(
        applicationCode: String?
    ): SdkConfig {
        return copy(
            applicationCode = applicationCode
        )
    }
}