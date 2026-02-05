package com.emarsys.core.url

import com.emarsys.mobileengage.action.models.OpenExternalUrlActionModel

interface ExternalUrlOpenerApi {
    suspend fun open(actionModel: OpenExternalUrlActionModel)
}
