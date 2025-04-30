package com.emarsys.api.predict.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
interface Logic {
    val logicName: String
    val data: Map<String, String>
    val variants: List<String>
}