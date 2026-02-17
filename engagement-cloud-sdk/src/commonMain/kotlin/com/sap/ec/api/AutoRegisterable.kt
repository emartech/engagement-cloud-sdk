package com.sap.ec.api

interface AutoRegisterable {
    suspend fun registerOnContext()
}