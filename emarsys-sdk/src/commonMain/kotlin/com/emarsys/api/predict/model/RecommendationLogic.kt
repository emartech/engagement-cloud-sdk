package com.emarsys.api.predict.model

import kotlinx.serialization.Serializable

@Serializable
class RecommendationLogic internal constructor(
    override val logicName: String,
    override val data: Map<String, String> = mapOf(),
    override val variants: List<String> = listOf()
) : Logic {

    companion object {
        const val SEARCH = "SEARCH"
        const val CART = "CART"
        const val RELATED = "RELATED"
        const val CATEGORY = "CATEGORY"
        const val ALSO_BOUGHT = "ALSO_BOUGHT"
        const val POPULAR = "POPULAR"
        const val PERSONAL = "PERSONAL"
        const val HOME = "HOME"

        fun search(): Logic {
            return RecommendationLogic(SEARCH, mapOf(), listOf())
        }

        fun search(searchTerm: String): Logic {
            val data: Map<String, String> = mapOf(
                "q" to searchTerm
            )
            return RecommendationLogic(SEARCH, data, listOf())
        }

        fun cart(): Logic {
            return RecommendationLogic(CART, mapOf(), listOf())
        }

        fun cart(cartItems: List<CartItem>): Logic {
            val data = mapOf(
                "cv" to "1",
                "ca" to cartItemsToQueryParam(cartItems)
            )

            return RecommendationLogic(CART, data, listOf())
        }

        fun related(): Logic {
            return RecommendationLogic(RELATED, mapOf(), listOf())
        }

        fun related(itemId: String): Logic {
            val data = mapOf("v" to "i:$itemId")

            return RecommendationLogic(RELATED, data, listOf())
        }

        fun category(): Logic {
            return RecommendationLogic(CATEGORY, mapOf(), listOf())
        }

        fun category(categoryPath: String): Logic {
            val data = mapOf("vc" to categoryPath)

            return RecommendationLogic(CATEGORY, data, listOf())
        }

        fun alsoBought(): Logic {
            return RecommendationLogic(ALSO_BOUGHT, mapOf(), listOf())
        }

        fun alsoBought(itemId: String): Logic {
            val data = mapOf("v" to "i:$itemId")
            return RecommendationLogic(ALSO_BOUGHT, data, listOf())
        }

        fun popular(): Logic {
            return RecommendationLogic(POPULAR, mapOf(), listOf())
        }

        fun popular(categoryPath: String): Logic {
            val data = mapOf("vc" to categoryPath)

            return RecommendationLogic(POPULAR, data, listOf())
        }

        fun personal(variants: List<String>? = listOf()): Logic {
            return RecommendationLogic(PERSONAL, mapOf(), variants!!)
        }

        fun home(variants: List<String>? = listOf()): Logic {
            return RecommendationLogic(HOME, mapOf(), variants!!)
        }

        private fun cartItemsToQueryParam(items: List<CartItem>): String {
            val sb = StringBuilder()
            for (i in items.indices) {
                if (i != 0) {
                    sb.append("|")
                }
                sb.append(cartItemToQueryParam(items[i]))
            }
            return sb.toString()
        }

        private fun cartItemToQueryParam(cartItem: CartItem?): String {
            return "i:" + cartItem!!.itemId + ",p:" + cartItem.price + ",q:" + cartItem.quantity
        }
    }

    override fun toString(): String {
        return "LogicName: $logicName, data: $data, variants: $variants"
    }
}