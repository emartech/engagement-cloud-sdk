package com.sap.ec.core.url

import com.sap.ec.mobileengage.action.models.OpenExternalUrlActionModel

interface ExternalUrlOpenerApi {
    suspend fun open(actionModel: OpenExternalUrlActionModel)
}
