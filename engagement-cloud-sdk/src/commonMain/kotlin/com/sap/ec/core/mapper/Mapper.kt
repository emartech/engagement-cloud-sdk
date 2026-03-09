package com.sap.ec.core.mapper

import com.sap.ec.InternalSdkApi

//needs to be exposed for ServiceWorker
@InternalSdkApi
interface Mapper<From, To> {

    suspend fun map(from: From): To?

}
