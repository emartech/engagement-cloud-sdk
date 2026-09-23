package com.sap.ec.networking.clients.recommendation

import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.recommendation.Feature
import com.sap.ec.recommendation.Product
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.serializer

class RecommendationResponseMapper(
    val json: Json,
    val sdkLogger: Logger
) : RecommendationResponseMapperApi {
    override suspend fun map(from: Response): List<Product> {
        val jsonResponse = json.parseToJsonElement(from.bodyAsText).jsonObject

        val features: Map<String, Feature> = extractFeatures(jsonResponse)
        val cohort = jsonResponse["cohort"]?.jsonPrimitive?.contentOrNull ?: throw RuntimeException(
            "Cohort is missing from the recommendation response"
        )

        return jsonResponse["products"]?.jsonObject?.let { productsObject ->
            productsObject.values.mapNotNull { productObject ->
                try {
                    val product = mapProduct(productObject)
                    val feature: String = extractFeature(features, product)
                        ?: throw RuntimeException("No relevant feature found for productId")
                    product.copy(feature = feature, cohort = cohort)
                } catch (e: Exception) {
                    sdkLogger.error("Mapping failed for response: $jsonResponse", e)
                    null
                }
            }
        } ?: emptyList()
    }

    private fun extractFeature(
        features: Map<String, Feature>,
        product: Product
    ): String? = features.filter { entry ->
        entry.value.items.map { it.id }.contains(product.productId)
    }.keys.firstOrNull()

    private fun extractFeatures(jsonResponse: JsonObject): Map<String, Feature> =
        jsonResponse["features"]?.jsonObject?.let { featuresObject ->
            val featuresMap = featuresObject.keys.associate { key ->
                featuresObject[key]?.let { feature ->
                    key to json.decodeFromJsonElement(Feature.serializer(), feature)
                } ?: throw RuntimeException("Feature is null for key $key")
            }
            if (featuresMap.isEmpty()) {
                throw RuntimeException("Features are empty")
            }
            featuresMap
        } ?: throw RuntimeException("Features key is missing")

    private fun mapProduct(
        productObject: JsonElement
    ): Product {
        val properties: List<String> = productProperties()
        val product: Product = json.decodeFromJsonElement(productObject)
        val customFields: Map<String, String?> = extractCustomFields(productObject, properties)
        return product.copy(customFields = customFields)
    }

    private fun productProperties(): List<String> {
        val descriptor = serializer<Product>().descriptor
        return (0 until descriptor.elementsCount).map {
            descriptor.getElementName(it)
        }
    }

    private fun extractCustomFields(
        productObject: JsonElement,
        properties: List<String>
    ): Map<String, String?> = productObject.jsonObject.filter { entry ->
        !properties.contains(entry.key)
    }.map { entry ->
        entry.key to entry.value.jsonPrimitive.contentOrNull
    }.associate { it.first to it.second }
}