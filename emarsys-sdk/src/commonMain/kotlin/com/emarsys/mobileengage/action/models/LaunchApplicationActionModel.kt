package com.emarsys.mobileengage.action.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface LaunchApplicationActionModel

@Serializable
@SerialName("LaunchApplication")
data object BasicLaunchApplicationActionModel : BasicActionModel(), LaunchApplicationActionModel
