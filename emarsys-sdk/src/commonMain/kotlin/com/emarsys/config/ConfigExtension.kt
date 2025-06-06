package com.emarsys.config

import com.emarsys.core.exceptions.PreconditionFailedException
import com.emarsys.core.log.Logger

suspend fun SdkConfig.isValid(logger: Logger): Boolean {
    val invalidCases = listOf("null", "", "0", "test")
    applicationCode?.let {
        ApplicationCode(it.uppercase()).validate(logger)
    }
    if (invalidCases.contains(merchantId)) {
        throw PreconditionFailedException("MerchantId should be valid!")
    }
    if (applicationCode == null && merchantId == null) {
        throw PreconditionFailedException("ApplicationCode or MerchantId must be present for Tracking!")
    }
    return true
}