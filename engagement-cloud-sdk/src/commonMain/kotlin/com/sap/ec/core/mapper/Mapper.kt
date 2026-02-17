package com.sap.ec.core.mapper

interface Mapper<From, To> {

    suspend fun map(from: From): To?

}
