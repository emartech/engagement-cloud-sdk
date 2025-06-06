package com.emarsys

import com.emarsys.config.SdkConfig
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName


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
