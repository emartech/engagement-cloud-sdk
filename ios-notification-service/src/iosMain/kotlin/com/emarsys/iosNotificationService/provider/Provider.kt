package com.emarsys.iosNotificationService.provider

interface Provider<Value> {

    fun provide(): Value

}
