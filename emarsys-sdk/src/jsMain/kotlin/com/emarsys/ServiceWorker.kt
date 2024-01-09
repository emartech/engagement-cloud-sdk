package com.emarsys

@OptIn(ExperimentalJsExport::class)
@JsExport
object ServiceWorker {
    fun register() {
        console.log("ServiceWorker.register")
    }
}