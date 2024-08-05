package com.emarsys.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface ButtonClickedActionModel {
    val id: String
    val buttonId: String
}

@Serializable
@SerialName("buttonClicked")
data class BasicButtonClickedActionModel(
    override val id: String,
    override val buttonId: String
): BasicActionModel(), ButtonClickedActionModel