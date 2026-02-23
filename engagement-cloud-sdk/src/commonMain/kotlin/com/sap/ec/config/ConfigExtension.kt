package com.sap.ec.config

import com.sap.ec.core.log.Logger

suspend fun SdkConfig.isValid(logger: Logger): Boolean {
    applicationCode.let {
        ApplicationCode(it.uppercase()).validate(logger)
    }
    return true
}