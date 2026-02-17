package com.sap.ec.core.actions.badge

import com.sap.ec.mobileengage.action.models.BadgeCount

interface BadgeCountHandlerApi {

    suspend fun handle(badgeCount: BadgeCount)

}