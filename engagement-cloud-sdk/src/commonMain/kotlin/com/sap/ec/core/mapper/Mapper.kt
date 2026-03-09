package com.sap.ec.core.mapper

import com.sap.ec.InternalSdkApi

@InternalSdkApi
interface Mapper<From, To> {

    suspend fun map(from: From): To?

}
