package com.emarsys.mobileengage.action.models

import kotlinx.serialization.Serializable

@Serializable
data class AppEventActionModel(
    override val type: String,
    val name: String,
    val payload: Map<String, String>?
): ActionModel(), OnEventActionModel, SilentPushActionModel, InAppActionModel, PushActionModel
