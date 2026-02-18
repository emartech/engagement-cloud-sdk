package com.sap.ec.config

import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import kotlin.jvm.JvmInline

@JvmInline
value class ApplicationCode(val value: String)

private val applicationCodeValidationRegex = "^[A-Z0-9]+-[A-Z0-9]+\$".toRegex()
private val multiRegionApplicationCodeValidationRegex =
    "^INS-[A-Z0-9]+-APP-[A-Z0-9]{5}\$".toRegex()

suspend fun ApplicationCode.validate(logger: Logger) {
    logger.debug("applicationCode validation")
    if (this.value.isBlank()) {
        val exception =
            SdkException.InvalidApplicationCodeException("Application code is empty")
        logger.error("Application code is empty")
        throw exception
    } else if (
        !this.value.matches(applicationCodeValidationRegex) &&
        !this.value.matches(multiRegionApplicationCodeValidationRegex)
    ) {
        val exception =
            SdkException.InvalidApplicationCodeException("Invalid application code: ${this.value}")
        logger.error("Invalid application code: ${this.value}", exception)
        throw exception
    }
}