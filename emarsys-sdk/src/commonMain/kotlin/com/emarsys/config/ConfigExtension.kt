package com.emarsys.config

import com.emarsys.core.exceptions.PreconditionFailedException
import com.emarsys.core.log.Logger

suspend fun SdkConfig.isValid(logger: Logger): Boolean {
    applicationCode?.let {
        ApplicationCode(it.uppercase()).validate(logger)
    }
    if (applicationCode == null) {
        throw PreconditionFailedException("ApplicationCode must be present for Tracking!")
    }
    return true
}