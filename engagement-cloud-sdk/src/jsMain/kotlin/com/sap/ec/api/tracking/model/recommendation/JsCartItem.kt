package com.sap.ec.api.tracking.model.recommendation

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("CartItem")
interface JsCartItem {
    val itemId: String
    val price: Double
    val quantity: Double
}