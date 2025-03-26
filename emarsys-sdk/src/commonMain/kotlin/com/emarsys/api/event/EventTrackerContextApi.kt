package com.emarsys.api.event

internal interface EventTrackerContextApi {
    val calls: MutableList<EventTrackerCall>
}