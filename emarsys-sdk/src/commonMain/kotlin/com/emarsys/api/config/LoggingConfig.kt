package com.emarsys.api.config

import com.emarsys.core.log.LogEntry
import com.emarsys.core.log.Logger

class LoggingConfig(private val logger: Logger): ConfigInstance {
    override suspend fun changeApplicationCode(applicationCode: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::changeApplicationCode.name, mapOf(
                "applicationCode" to applicationCode,
            )
        )
        logger.debug(entry)
    }

    override suspend fun changeMerchantId(merchantId: String) {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::changeMerchantId.name, mapOf(
                "merchantId" to merchantId,
            )
        )
        logger.debug(entry)
    }

    override suspend fun activate() {
        val entry = LogEntry.createMethodNotAllowed(this, this::activate.name)
        logger.debug(entry)
    }
}