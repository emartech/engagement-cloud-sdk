package com.sap.ec.mobileengage.inapp.jsbridge

import com.sap.ec.mobileengage.inapp.presentation.InAppType

internal data class InAppJsBridgeData(
    val dismissId: String,
    val trackingInfo: String,
    val inAppType: InAppType
)