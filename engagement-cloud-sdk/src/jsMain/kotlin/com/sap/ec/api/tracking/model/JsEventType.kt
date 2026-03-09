package com.sap.ec.api.tracking.model

import kotlinx.serialization.Serializable

@OptIn(ExperimentalJsExport::class)
@Serializable
internal enum class JsEventType {
    CUSTOM,
    NAVIGATE
}