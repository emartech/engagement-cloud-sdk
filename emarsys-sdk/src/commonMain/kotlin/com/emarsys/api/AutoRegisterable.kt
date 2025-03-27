package com.emarsys.api

interface AutoRegisterable {
    suspend fun registerOnContext()
}