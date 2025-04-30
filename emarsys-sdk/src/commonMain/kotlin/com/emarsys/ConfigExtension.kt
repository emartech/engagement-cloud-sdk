package com.emarsys

import com.emarsys.core.exceptions.PreconditionFailedException

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