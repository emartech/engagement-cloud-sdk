package com.sap.ec.api.tracking.model

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