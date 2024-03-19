package com.emarsys.mobileengage.action.models

import kotlinx.serialization.Serializable

@Serializable
data class CustomEventActionModel(
    override val type: String,
    val name: String,
    val payload: Map<String, String>?
): ActionModel(), OnEventActionModel, InAppActionModel, PushActionModel
