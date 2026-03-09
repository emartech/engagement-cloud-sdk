package com.sap.ec.api

import com.sap.ec.InternalSdkApi

@InternalSdkApi
interface AutoRegisterable {
    suspend fun registerOnContext()
}