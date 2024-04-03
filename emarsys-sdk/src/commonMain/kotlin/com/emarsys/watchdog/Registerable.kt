package com.emarsys.watchdog

interface Registerable {
    suspend fun register()
}