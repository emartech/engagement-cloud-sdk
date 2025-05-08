package com.emarsys.api.events

import kotlinx.coroutines.Job

@OptIn(ExperimentalJsExport::class)
@JsExport
external interface SdkEventSubscriptionApi {

    fun unsubscribe()

}

class SdkEventSubscription(private val job: Job) : SdkEventSubscriptionApi {

    override fun unsubscribe() {
        job.cancel()
    }

}