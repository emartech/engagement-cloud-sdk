package com.emarsys.core.badge

interface BadgeCountHandlerApi {

    suspend fun add(increment: Int)

    suspend fun set(value: Int)

}