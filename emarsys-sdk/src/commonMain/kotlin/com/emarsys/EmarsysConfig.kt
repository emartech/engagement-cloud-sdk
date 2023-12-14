package com.emarsys

import com.emarsys.core.exceptions.PreconditionFailed


data class EmarsysConfig(val applicationCode: String? = null, val merchantId: String? = null)

fun EmarsysConfig.isValid(): Boolean {
    val invalidCases = listOf("null", "", "0", "test")
    if (invalidCases.contains(applicationCode)) {
        throw PreconditionFailed("ApplicationCode should be valid!")
    }
    if (invalidCases.contains(merchantId)) {
        throw PreconditionFailed("MerchantId should be valid!")
    }
    if (applicationCode == null && merchantId == null) {
        throw PreconditionFailed("ApplicationCode or MerchantId must be present for Tracking!")
    }
    return true
}