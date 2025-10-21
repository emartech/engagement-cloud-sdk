package com.emarsys.api.events

import EmarsysSdkEventListener

@OptIn(ExperimentalJsExport::class)
@JsExport
interface EventEmitterApi {
    fun on(event: String, listener: EmarsysSdkEventListener)

    fun once(event: String, listener: EmarsysSdkEventListener)

    fun off(event: String, listener: EmarsysSdkEventListener)

    fun removeAllListeners()
}