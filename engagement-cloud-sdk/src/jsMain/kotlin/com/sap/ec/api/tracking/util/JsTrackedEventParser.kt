package com.sap.ec.api.tracking.util

import com.sap.ec.api.tracking.model.JsEventValidationData
import com.sap.ec.api.tracking.model.JsTrackedEvent
import com.sap.ec.util.JsonUtil
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromDynamic


@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified T : JsEventValidationData> validateJsEvent(event: JsTrackedEvent) {
    JsonUtil.json.decodeFromDynamic<T>(event)
}

internal inline fun <reified ValidationData : JsEventValidationData, JSTrackedEventType : JsTrackedEvent> JsTrackedEvent.parseWithValidation(): JSTrackedEventType {
    validateJsEvent<ValidationData>(this)
    return this.unsafeCast<JSTrackedEventType>()
}

@OptIn(ExperimentalSerializationApi::class)
internal fun parseAttributesMap(attributes: dynamic): Map<String, String> =
    try {
        JsonUtil.json.decodeFromDynamic<Map<String, String>>(attributes)
    } catch (e: Exception) {
        throw IllegalArgumentException("Failed to parse attributes map.", e)
    }
