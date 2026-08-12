package com.sap.ec.config

import com.sap.ec.core.log.Logger

internal suspend fun SdkConfig.isValid(logger: Logger, globalRemoteConfigApplicationCodeValidationRegex: Regex?): Boolean {
    ApplicationCode(applicationCode.uppercase()).validate(logger, globalRemoteConfigApplicationCodeValidationRegex)
    return true
}