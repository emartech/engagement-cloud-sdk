package com.sap.ec.api.event

internal interface EventTrackerContextApi {
    val calls: MutableList<EventTrackerCall>
}