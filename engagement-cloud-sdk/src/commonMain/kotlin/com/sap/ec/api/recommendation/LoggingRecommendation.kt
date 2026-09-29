package com.sap.ec.api.recommendation

import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.Product
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class LoggingRecommendation(private val logger: Logger) : RecommendationInstance {
    override suspend fun requestRecommendation(event: SdkEvent): List<Product> {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::requestRecommendation.name, buildJsonObject {
                put("event", JsonPrimitive(event.toString()))
            }
        )
        logger.debug(entry)
        return emptyList()
    }

    override suspend fun activate() {
        logger.debug("${this::class.simpleName} activated")
    }
}