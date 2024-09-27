package com.emarsys.core.device

interface UIDeviceApi {

    fun osVersion(): String
    fun deviceModel(): String

    infix fun hasOsVersionAtLeast(majorVersion: Int): Boolean
}