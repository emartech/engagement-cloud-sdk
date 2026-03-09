package com.sap.ec.api.generic

internal interface ApiContext<CallType> {

    val calls: MutableList<CallType>

}