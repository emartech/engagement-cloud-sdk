package com.emarsys.mobileengage.action.models

import kotlinx.serialization.Serializable

@Serializable
sealed class ActionModel {
    abstract val type: String // TODO: this should not be here after mapping
}

sealed interface InAppActionModel
sealed interface PushActionModel
sealed interface SilentPushActionModel
sealed interface OnEventActionModel
