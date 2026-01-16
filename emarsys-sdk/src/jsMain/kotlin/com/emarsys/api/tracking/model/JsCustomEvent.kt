package com.emarsys.api.tracking.model

/**
 * Represents a custom event that can be tracked using the SDK.
 *
 * @property name The name of the custom event.
 * @property attributes A map of string key-value pairs representing additional attributes for the event.
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsCustomEvent {
    val name: String

    val attributes: dynamic
}