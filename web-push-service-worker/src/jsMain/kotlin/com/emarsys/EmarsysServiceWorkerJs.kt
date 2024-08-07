package com.emarsys

import web.serviceworker.ServiceWorkerGlobalScope
import web.events.EventType
import web.push.PushEvent

external var self: ServiceWorkerGlobalScope

fun main() {
    self.addEventListener(EventType("push"), { event: PushEvent ->
        EmarsysServiceWorker().onPush(event)
        console.log("push")
    })

    self.addEventListener(EventType("install"), {
        console.log("install")
    })
}