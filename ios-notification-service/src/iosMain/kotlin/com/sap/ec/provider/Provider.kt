package com.sap.ec.iosNotificationService.provider

interface Provider<Value> {

    fun provide(): Value

}
