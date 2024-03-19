package com.emarsys.mobileengage.action.models

import kotlinx.serialization.Serializable

@Serializable
data class OpenExternalUrlActionModel(
    override val type: String,
    val url: String
): ActionModel(), InAppActionModel, PushActionModel
