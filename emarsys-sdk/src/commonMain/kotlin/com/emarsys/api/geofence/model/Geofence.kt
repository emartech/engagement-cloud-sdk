package com.emarsys.api.geofence.model

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Geofence(
    val id: String,
    val lat: Double,
    val lon: Double,
    val radius: Double,
    val waitInterval: Double?,
    val triggers: List<Trigger>
)