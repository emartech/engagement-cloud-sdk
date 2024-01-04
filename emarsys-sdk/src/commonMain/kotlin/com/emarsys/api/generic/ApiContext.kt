package com.emarsys.api.generic

interface ApiContext<CallType> {

    val calls: MutableList<CallType>

}