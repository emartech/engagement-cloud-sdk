package com.sap.ec.mobileengage.inapp.jsbridge

import com.sap.ec.api.event.model.EventSource
import com.sap.ec.mobileengage.inapp.presentation.InAppType

internal data class InAppJsBridgeData(
    val dismissId: String,
    val trackingInfo: String,
    val inAppType: InAppType
)

internal fun InAppJsBridgeData.toEventSource(): EventSource {
    return when (inAppType) {
        InAppType.OVERLAY, InAppType.RIBBON -> EventSource.InApp
        InAppType.INLINE -> EventSource.InlineInApp
        InAppType.EMBEDDED_MESSAGING -> EventSource.EmbeddedMessagingRichContent
    }
}