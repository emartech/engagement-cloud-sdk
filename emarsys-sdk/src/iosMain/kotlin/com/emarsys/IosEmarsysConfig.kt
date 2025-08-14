package com.emarsys

import com.emarsys.config.SdkConfig
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName

/**
 * Configuration class for enabling the Emarsys SDK on iOS.
 *
 * @property applicationCode The application code of your application.
 */
@Serializable
@OptIn(ExperimentalObjCName::class)
@ObjCName("EmarsysConfig")
data class IosEmarsysConfig(
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
