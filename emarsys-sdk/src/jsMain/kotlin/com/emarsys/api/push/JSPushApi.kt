package com.emarsys.api.push

import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSPushApi {
    fun registerPushToken(pushToken: String): Promise<Unit>
    fun clearPushToken(): Promise<Unit>
    fun getPushToken(): Promise<String?>
}