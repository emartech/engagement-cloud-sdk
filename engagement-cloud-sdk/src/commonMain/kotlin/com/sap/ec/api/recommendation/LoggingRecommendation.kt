package com.sap.ec.api.recommendation

import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.RecommendationResult
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class LoggingRecommendation(private val logger: Logger) : RecommendationInstance {
    override suspend fun requestRecommendation(event: SdkEvent): Map<String, RecommendationResult> {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::requestRecommendation.name, buildJsonObject {
                put("event", JsonPrimitive(event.toString()))
            }
        )
        logger.debug(entry)
        return mapOf()
    }

    override suspend fun activate() {
        logger.debug("${this::class.simpleName} activated")
    }
}