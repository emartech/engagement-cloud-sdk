package com.emarsys.api.push

import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JSPushApi {
    fun registerPushToken(pushToken: String): Promise<Any>
    fun clearPushToken(): Promise<Any>
    fun getPushToken(): Promise<String?>
}