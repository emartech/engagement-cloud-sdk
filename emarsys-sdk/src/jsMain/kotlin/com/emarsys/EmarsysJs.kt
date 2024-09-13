package com.emarsys

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.promise
import org.w3c.dom.Window
import kotlin.js.Promise

lateinit var emarsysWindow: Window

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("EmarsysJs")
class EmarsysJs {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())

    fun init(window: Window): Promise<Any> {
        emarsysWindow = window
        return coroutineScope.promise {
            Emarsys.initialize()
        }
    }

    fun enableTracking(jsEmarsysConfig: JsEmarsysConfig): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.enableTracking(jsEmarsysConfig)
            Emarsys.linkContact(2575, "test@test.com")
        }
    }

    fun trackCustomEvent(eventName: String, eventPayload: Map<String, String>?): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.trackCustomEvent(eventName, eventPayload)
        }
    }

}