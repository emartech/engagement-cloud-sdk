package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.recommendation.models.FeatureRecommendation
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.product.Feature
import com.sap.ec.recommendation.models.product.Product
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

class RecommendationResponseMapper(
    val json: Json,
    val sdkLogger: Logger
) : RecommendationResponseMapperApi {
    private val productProperties: List<String> = serializer<Product>().descriptor.let { d ->
        (0 until d.elementsCount).map { d.getElementName(it) }
    }

    override suspend fun map(from: Response): RecommendationResult {
        val response = try {
            json.decodeFromString<RecommendationResponse>(from.bodyAsText)
        } catch (_: Exception) {
            sdkLogger.info("Failed to parse recommendation response body, response: $from")
            return RecommendationResult(emptyMap())
        }
        val features = response.features

        if (features.isEmpty()) {
            sdkLogger.info("Features key is missing or features are empty in the recommendation response, response: $from")
            return RecommendationResult(emptyMap())
        }

        val products = buildProductMap(response)

        return RecommendationResult(buildResultMap(features, products, response.cohort))
    }

    private suspend fun buildProductMap(response: RecommendationResponse): Map<String, Product> =
        response.products.mapNotNull { (productId, productObject) ->
            try {
                val product = json.decodeFromJsonElement<Product>(productObject)
                productId to product.copy(customFields = extractCustomFields(productObject))
            } catch (_: Exception) {
                sdkLogger.info("Failed to parse product with key $productId")
                null
            }
        }.toMap()

    private suspend fun buildResultMap(
        features: Map<String, Feature>,
        products: Map<String, Product>,
        cohort: String?
    ): Map<String, FeatureRecommendation> =
        features.mapNotNull { (featureName, feature) ->
            if (feature.items == null) {
                sdkLogger.info("Feature $featureName has no items and was omitted")
                return@mapNotNull null
            }
            try {
                val featureProducts = feature.items.mapNotNull { item ->
                    products[item.id]?.copy(feature = featureName, cohort = cohort)
                }
                featureName to FeatureRecommendation(
                    featureName = featureName,
                    topicLabel = feature.topicLabel,
                    hasMore = feature.hasMore,
                    merchants = feature.merchants,
                    products = featureProducts
                )
            } catch (e: Exception) {
                sdkLogger.info("Mapping failed for feature: $featureName", e)
                null
            }
        }.toMap()

    private fun extractCustomFields(productObject: JsonObject): Map<String, String?> =
        productObject
            .filterKeys { it !in productProperties }
            .mapValues { it.value.jsonPrimitive.contentOrNull }
}