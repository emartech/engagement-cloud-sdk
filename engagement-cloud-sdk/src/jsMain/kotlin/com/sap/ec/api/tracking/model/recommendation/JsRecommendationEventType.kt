package com.sap.ec.api.tracking.model.recommendation

import kotlinx.serialization.Serializable

@OptIn(ExperimentalJsExport::class)
@Serializable
enum class JsRecommendationEventType {
    CATEGORY_VIEW,
    ITEM_VIEW,
    RECOMMENDATION_CLICK,
    SEARCH,
    CART,
    PURCHASE,
    TAG
}