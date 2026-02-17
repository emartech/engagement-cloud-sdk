package com.sap.ec.api.config

internal interface ConfigContextApi {
    val calls: MutableList<ConfigCall>
}