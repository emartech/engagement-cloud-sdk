package com.emarsys

import web.events.Event

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("EmarsysServiceWorker")
class EmarsysServiceWorker {

    fun onPush(event: Event) {
        console.log("Push event received: ", JSON.stringify(event))
    }
}