package com.sap.ec.api.recommendation

import com.sap.ec.recommendation.models.tracking.TrackedRecommendationEvent
import com.sap.ec.core.log.LogEntry
import com.sap.ec.core.log.Logger
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LoggingRecommendation(private val logger: Logger) : RecommendationInstance {
    override suspend fun requestRecommendation(
        recommendationOptions: RecommendationOptions,
        contextEvent: TrackedRecommendationEvent?
    ): RecommendationResult {
        val entry = LogEntry.createMethodNotAllowed(
            this, this::requestRecommendation.name, buildJsonObject {
                put("options", recommendationOptions.toString())
                put("event", contextEvent.toString())
            }
        )
        logger.debug(entry)
        return RecommendationResult(emptyMap())
    }

    override suspend fun activate() {
        logger.debug("${this::class.simpleName} activated")
    }
}