package com.sap.ec.mobileengage.action.actions

import com.sap.ec.core.url.ExternalUrlOpenerApi
import com.sap.ec.mobileengage.action.models.OpenExternalUrlActionModel

class OpenExternalUrlAction(
    private val action: OpenExternalUrlActionModel,
    private val externalUrlOpener: ExternalUrlOpenerApi
): Action<Unit> {

    override suspend fun invoke(value: Unit?) {
        externalUrlOpener.open(action)
    }
}
