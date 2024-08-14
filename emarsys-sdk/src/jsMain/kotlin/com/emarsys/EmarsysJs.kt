package com.emarsys

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.promise
import kotlin.js.Promise

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("EmarsysJs")
class EmarsysJs {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())

    fun enableTracking(jsEmarsysConfig: JsEmarsysConfig): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.initialize()
            Emarsys.enableTracking(jsEmarsysConfig)
        }
    }

    fun trackCustomEvent(eventName: String, eventPayload: Map<String, String>?): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.trackCustomEvent(eventName, eventPayload)
        }
    }

}