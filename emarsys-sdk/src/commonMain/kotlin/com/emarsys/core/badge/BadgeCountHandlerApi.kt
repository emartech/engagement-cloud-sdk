package com.emarsys.core.badge

import com.emarsys.mobileengage.action.models.BadgeCount

interface BadgeCountHandlerApi {

    suspend fun handle(badgeCount: BadgeCount)

    suspend fun add(increment: Int)

    suspend fun set(value: Int)

}