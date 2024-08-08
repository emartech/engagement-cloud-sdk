package com.emarsys

import web.events.EventType
import web.push.PushEvent

val worker = EmarsysServiceWorker()

fun main() {
    self.addEventListener(EventType("push"), { event: PushEvent ->
        val promise = worker.onPush(event)
        event.waitUntil(promise)
    })

    self.addEventListener(EventType("install"), {
        console.log("install")
    })

    self.addEventListener(EventType("notificationclick"), {
        console.log("notificationclick")
    })

    self.addEventListener(EventType("pushsubscriptionchange"), {
        console.log("pushsubscriptionchange")
    })
}