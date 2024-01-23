package com.emarsys.core.device

interface PlatformInfoCollectorApi {

    fun collect(): String

    fun applicationVersion(): String
}