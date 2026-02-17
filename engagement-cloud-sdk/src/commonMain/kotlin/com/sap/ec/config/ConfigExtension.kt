package com.sap.ec.config

import com.sap.ec.core.exceptions.SdkException.PreconditionFailedException
import com.sap.ec.core.log.Logger

suspend fun SdkConfig.isValid(logger: Logger): Boolean {
    applicationCode?.let {
        ApplicationCode(it.uppercase()).validate(logger)
    }
    if (applicationCode == null) {
        throw PreconditionFailedException("ApplicationCode must be present for Tracking!")
    }
    return true
}