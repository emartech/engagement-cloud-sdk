package com.emarsys.api.predict.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
interface CartItem {
    val itemId: String
    val price: Double
    val quantity: Double
}