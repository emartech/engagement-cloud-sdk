package com.sap.ec

import com.sap.ec.config.SdkConfig
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName

/**
 * Configuration class for enabling the Engagement Cloud SDK on iOS.
 *
 * @property applicationCode The application code of your application.
 */
@Serializable
@OptIn(ExperimentalObjCName::class)
@ObjCName("EngagementCloudConfig")
data class IosEngagementCloudSDKConfig(
    override val applicationCode: String? = null
): SdkConfig {
    override fun copyWith(
        applicationCode: String?
    ): SdkConfig {
        return copy(
            applicationCode = applicationCode
        )
    }
}
