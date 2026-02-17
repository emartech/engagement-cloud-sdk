package com.sap.ec.api.generic

interface ApiContext<CallType> {

    val calls: MutableList<CallType>

}