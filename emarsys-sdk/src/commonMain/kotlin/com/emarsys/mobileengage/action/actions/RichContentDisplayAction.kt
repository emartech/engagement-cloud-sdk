package com.emarsys.mobileengage.action.actions

import com.emarsys.mobileengage.action.models.BasicRichContentDisplayActionModel

class RichContentDisplayAction(
    private val action: BasicRichContentDisplayActionModel
): Action<Unit> {

    override suspend fun invoke(value: Unit?) {
    }
}