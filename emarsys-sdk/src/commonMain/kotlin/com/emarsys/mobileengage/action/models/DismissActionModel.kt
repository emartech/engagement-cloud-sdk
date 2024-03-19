package com.emarsys.mobileengage.action.models

import com.emarsys.core.message.MsgBox
import kotlinx.serialization.Serializable

@Serializable
data class DismissActionModel(
    override val type: String,
    var msgBox: MsgBox<Unit>? = null
): ActionModel(), InAppActionModel, PushActionModel
