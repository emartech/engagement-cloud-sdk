package com.emarsys.core.mapper

interface Mapper<From, To> {

    fun map(from: From): To

}
