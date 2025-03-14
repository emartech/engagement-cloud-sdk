package com.emarsys.core.actions.badge

import com.emarsys.mobileengage.action.models.BadgeCount

interface BadgeCountHandlerApi {

    suspend fun handle(badgeCount: BadgeCount)

}