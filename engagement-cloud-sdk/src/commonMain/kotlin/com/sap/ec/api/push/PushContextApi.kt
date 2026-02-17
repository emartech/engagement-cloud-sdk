package com.sap.ec.api.push

internal interface PushContextApi {
    val calls: MutableList<PushCall>
}