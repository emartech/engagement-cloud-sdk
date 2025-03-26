package com.emarsys.api.push

internal interface PushContextApi {
    val calls: MutableList<PushCall>
}