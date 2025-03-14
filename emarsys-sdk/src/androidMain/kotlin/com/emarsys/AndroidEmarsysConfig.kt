package com.emarsys

data class AndroidEmarsysConfig(
    override val applicationCode: String? = null,
    override val merchantId: String? = null,
    override val sharedSecret: String? = null,
    val launchActivityClass: Class<*>? = null
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
