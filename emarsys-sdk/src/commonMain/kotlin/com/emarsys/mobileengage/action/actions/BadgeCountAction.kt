package com.emarsys.mobileengage.action.actions

import com.emarsys.core.badge.BadgeCountHandlerApi
import com.emarsys.mobileengage.action.models.BadgeCountActionModel

class BadgeCountAction(
    private val action: BadgeCountActionModel,
    private val badgeCountHandler: BadgeCountHandlerApi
): Action<Unit> {
    override suspend fun invoke(value: Unit?) {
        if (action.method.lowercase() == "add") {
            badgeCountHandler.add(action.value)
        } else {
            badgeCountHandler.set(action.value)
        }
    }
}
