package com.emarsys.core.mapper

interface Mapper<From, To> {

    suspend fun map(from: From): To?

}
