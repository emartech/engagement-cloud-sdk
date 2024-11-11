package com.emarsys.mobileengage.events

data class SdkEvent(val source: SdkEventSource, val name: String, val payload: Map<String, Any>?)