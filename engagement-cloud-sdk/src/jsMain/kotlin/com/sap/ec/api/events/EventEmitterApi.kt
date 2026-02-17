package com.sap.ec.api.events

import EngagementCloudSdkEventListener

@OptIn(ExperimentalJsExport::class)
@JsExport
interface EventEmitterApi {
    fun on(event: String, listener: EngagementCloudSdkEventListener)

    fun once(event: String, listener: EngagementCloudSdkEventListener)

    fun off(event: String, listener: EngagementCloudSdkEventListener)

    fun removeAllListeners()
}