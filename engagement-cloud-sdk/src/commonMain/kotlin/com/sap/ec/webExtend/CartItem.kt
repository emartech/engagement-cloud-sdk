package com.sap.ec.webExtend

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val itemId: String,
    val price: Double,
    val quantity: Double,
)
