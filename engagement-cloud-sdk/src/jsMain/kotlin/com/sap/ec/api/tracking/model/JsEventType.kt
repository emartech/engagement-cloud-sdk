package com.sap.ec.api.tracking.model

import kotlinx.serialization.Serializable

@OptIn(ExperimentalJsExport::class)
@Serializable
enum class JsEventType {
    CUSTOM,
    NAVIGATE
}