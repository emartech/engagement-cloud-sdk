package com.emarsys.mobileengage.badge

import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.mobileengage.action.models.BadgeCount

class AndroidBadgeCountHandler: BadgeCountHandlerApi {
    override suspend fun handle(badgeCount: BadgeCount) {
        TODO("Not yet implemented")
    }

    override suspend fun add(increment: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun set(value: Int) {
        TODO("Not yet implemented")
    }

}