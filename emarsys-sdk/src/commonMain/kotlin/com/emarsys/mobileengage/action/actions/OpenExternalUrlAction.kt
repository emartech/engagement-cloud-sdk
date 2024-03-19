package com.emarsys.mobileengage.action.actions

import com.emarsys.core.url.ExternalUrlOpenerApi
import com.emarsys.mobileengage.action.models.OpenExternalUrlActionModel

class OpenExternalUrlAction(
    private val action: OpenExternalUrlActionModel,
    private val externalUrlOpener: ExternalUrlOpenerApi
): Action<Unit> {

    override suspend fun invoke(value: Unit?) {
        externalUrlOpener.open(action.url)
    }
}
