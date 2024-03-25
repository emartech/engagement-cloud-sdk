package com.emarsys.mobileengage.action.models

import kotlinx.serialization.Serializable

@Serializable
data class DismissActionModel(
    override val type: String,
    var topic: String? = null,
): ActionModel(), InAppActionModel, PushActionModel
