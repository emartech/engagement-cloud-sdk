package com.emarsys

object Emarsys {
    suspend fun initialize() {}

    suspend fun enableTracking(config: EmarsysConfig) {
        config.isValid()
    }
}