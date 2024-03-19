package com.emarsys.mobileengage.action.models

import kotlinx.serialization.Serializable

@Serializable
data class AskForPushPermissionActionModel(
    override val type: String
): ActionModel(), InAppActionModel
