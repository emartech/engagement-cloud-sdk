package com.emarsys

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.promise
import kotlin.js.Promise

fun main() {
    EmarsysJs().init()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("EmarsysJs")
class EmarsysJs {

    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())

    fun init(): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.initialize()
        }
    }

    fun enableTracking(jsEmarsysConfig: JsEmarsysConfig): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.enableTracking(jsEmarsysConfig)
        }
    }

    fun linkContact(contactFieldId: Int, contactFieldValue: String): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.linkContact(contactFieldId, contactFieldValue)
        }
    }

    fun trackCustomEvent(eventName: String, eventPayload: Map<String, String>?): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.trackCustomEvent(eventName, eventPayload)
        }
    }

}