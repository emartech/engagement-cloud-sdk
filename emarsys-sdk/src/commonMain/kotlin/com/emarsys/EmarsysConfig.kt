package com.emarsys

import com.emarsys.core.exceptions.PreconditionFailedException
import kotlinx.serialization.Serializable


@Serializable
data class EmarsysConfig(
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

fun SdkConfig.isValid(): Boolean {
    val invalidCases = listOf("null", "", "0", "test")
    if (invalidCases.contains(applicationCode)) {
        throw PreconditionFailedException("ApplicationCode should be valid!")
    }
    if (invalidCases.contains(merchantId)) {
        throw PreconditionFailedException("MerchantId should be valid!")
    }
    if (applicationCode == null && merchantId == null) {
        throw PreconditionFailedException("ApplicationCode or MerchantId must be present for Tracking!")
    }
    return true
}