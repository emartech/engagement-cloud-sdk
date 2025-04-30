package com.emarsys.api.inapp

import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSInAppApi {
    val isPaused:Boolean
    fun pause(): Promise<Unit>
    fun resume(): Promise<Unit>
}