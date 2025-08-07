package com.emarsys

import com.emarsys.config.SdkConfig
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName

/**
 * Configuration class for enabling the Emarsys SDK on iOS.
 *
 * @property applicationCode The application code of your application.
 * @property merchantId The merchant ID.
 */
@Serializable
@OptIn(ExperimentalObjCName::class)
@ObjCName("EmarsysConfig")
data class IosEmarsysConfig(
    override val applicationCode: String? = null,
    override val merchantId: String? = null,
    override val sharedSecret: String? = null
): SdkConfig {
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
