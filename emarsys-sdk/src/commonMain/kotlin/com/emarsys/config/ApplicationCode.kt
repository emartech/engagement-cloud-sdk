package com.emarsys.config

import com.emarsys.core.exceptions.SdkException
import com.emarsys.core.log.Logger
import kotlin.jvm.JvmInline

@JvmInline
value class ApplicationCode(val value: String)

suspend fun ApplicationCode.validate(logger: Logger) {
    val appCodeValidationRegex = "^[A-Z0-9]+(?:-[A-Z0-9]+)+\$"
    logger.debug("applicationCode validation")
    if (this.value.isBlank()) {
        val exception =
            SdkException.InvalidApplicationCodeException("Application code is empty")
        logger.error("Application code is empty")
        throw exception
    } else if (!this.value.matches(appCodeValidationRegex.toRegex())) {
        val exception =
            SdkException.InvalidApplicationCodeException("Invalid application code: $this.value")
        logger.error("Invalid application code: $this.value", exception)
        throw exception
    }
}