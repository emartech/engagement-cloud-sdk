package com.sap.ec.mobileengage.action.actions

import com.sap.ec.mobileengage.action.models.BasicRichContentDisplayActionModel

class RichContentDisplayAction(
    private val action: BasicRichContentDisplayActionModel
): Action<Unit> {

    override suspend fun invoke(value: Unit?) {
    }
}