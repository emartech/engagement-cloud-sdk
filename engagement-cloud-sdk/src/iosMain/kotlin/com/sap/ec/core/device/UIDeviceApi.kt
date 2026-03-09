package com.sap.ec.core.device

internal interface UIDeviceApi {

    fun osVersion(): String
    fun deviceModel(): String

    infix fun hasOsVersionAtLeast(majorVersion: Int): Boolean
}