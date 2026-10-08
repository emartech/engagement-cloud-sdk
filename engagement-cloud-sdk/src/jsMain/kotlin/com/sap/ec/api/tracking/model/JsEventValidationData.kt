package com.sap.ec.api.tracking.model

import com.sap.ec.api.tracking.model.recommendation.JSCategoryViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartEvent
import com.sap.ec.api.tracking.model.recommendation.JsCartItem
import com.sap.ec.api.tracking.model.recommendation.JsItemViewEvent
import com.sap.ec.api.tracking.model.recommendation.JsPurchaseEvent
import com.sap.ec.api.tracking.model.recommendation.JsSearchEvent
import com.sap.ec.api.tracking.model.recommendation.JsTagEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal sealed interface JsEventValidationData

@Serializable
@SerialName("CUSTOM")
internal data class JsCustomEventValidationData(
    override val name: String,
    @Transient
    override val attributes: dynamic = null
) : JsCustomEvent, JsEventValidationData {
    override val type: String = JsEventType.CUSTOM.name

}

@Serializable
@SerialName("NAVIGATE")
internal data class JsNavigateEventValidationData(
    override val location: String,
) : JsNavigateEvent, JsEventValidationData {
    override val type: String = JsEventType.NAVIGATE.name

}

@Serializable
@SerialName("RECOMMENDATION")
internal data class JsRecommendationEventValidationData(
    override val recommendationEventType: String,
) : JsRecommendationEvent, JsEventValidationData {
    override val type: String = JsEventType.RECOMMENDATION.name
}

@Serializable
@SerialName("ITEM_VIEW")
internal data class JsItemViewEventValidationData(
    override val itemId: String
) : JsItemViewEvent, JsEventValidationData

@Serializable
@SerialName("CART")
internal data class JsCartEventValidationData(
    @Transient
    override val items: Array<JsCartItem> = arrayOf()
) : JsCartEvent, JsEventValidationData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as JsCartEventValidationData

        return items.contentEquals(other.items)
    }

    override fun hashCode(): Int {
        return items.contentHashCode()
    }
}

@Serializable
@SerialName("CATEGORY_VIEW")
internal data class JsCategoryViewEventValidationData(
    override val categoryPath: String
) : JSCategoryViewEvent, JsEventValidationData

@Serializable
@SerialName("PURCHASE")
internal data class JsPurchaseEventValidationData(
    override val orderId: String,
    @Transient
    override val items: Array<JsCartItem> = arrayOf()
) : JsPurchaseEvent, JsEventValidationData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as JsPurchaseEventValidationData

        if (orderId != other.orderId) return false
        if (!items.contentEquals(other.items)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = orderId.hashCode()
        result = 31 * result + items.contentHashCode()
        return result
    }
}

@Serializable
@SerialName("SEARCH")
internal data class JsSearchEventValidationData(
    override val searchTerm: String
) : JsSearchEvent, JsEventValidationData

@Serializable
@SerialName("SEARCH")
internal data class JsTagEventValidationData(
    override val tag: String,
    @Transient
    override val attributes: dynamic = null
) : JsTagEvent, JsEventValidationData