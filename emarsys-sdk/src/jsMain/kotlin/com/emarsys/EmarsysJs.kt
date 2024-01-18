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

    fun enableTracking(): Promise<Any> {
        return coroutineScope.promise {
            Emarsys.initialize()
            Emarsys.enableTracking(EmarsysConfig("EMS8B-0891D"))

        }
    }

}