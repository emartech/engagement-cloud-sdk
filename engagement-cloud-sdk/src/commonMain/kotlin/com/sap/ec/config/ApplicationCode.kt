package com.sap.ec.config

import com.sap.ec.core.exceptions.SdkException
import com.sap.ec.core.log.Logger
import kotlin.jvm.JvmInline

@JvmInline
internal value class ApplicationCode(val value: String)

private val applicationCodeValidationRegex = "^[A-Z0-9]+-[A-Z0-9]+\$".toRegex()
private val multiRegionApplicationCodeValidationRegexV1 =
    "^INS-[A-Z0-9]+-APP-[A-Z0-9]{5}\$".toRegex()
private val multiRegionApplicationCodeValidationRegexV2 =
    "^[SP]-[A-Z]{2}[0-9]{3}-[0-9A-Z]{5}\$".toRegex()

internal suspend fun ApplicationCode.validate(
    logger: Logger,
    globalRemoteConfigApplicationCodeValidationRegex: Regex?
) {
    logger.debug("applicationCode validation")
    if (this.value.isBlank()) {
        val exception =
            SdkException.InvalidApplicationCodeException("Application code is empty")
        logger.error("Application code is empty")
        throw exception
    }

    if (this.value.matches(applicationCodeValidationRegex) ||
        globalRemoteConfigApplicationCodeValidationRegex?.let { this.value.matches(it) } == true ||
        this.value.matches(multiRegionApplicationCodeValidationRegexV1) ||
        this.value.matches(multiRegionApplicationCodeValidationRegexV2)
    ) {
        return
    }

    val exception =
        SdkException.InvalidApplicationCodeException("Invalid application code: ${this.value}")
    logger.error("Invalid application code: ${this.value}", exception)
    throw exception
}