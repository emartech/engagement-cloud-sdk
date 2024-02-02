package com.emarsys.mobileengage.action

import kotlinx.serialization.Serializable

@Serializable
sealed interface Action

@Serializable
sealed class ActionModel : Action {
    abstract val id: String
    abstract val title: String
    abstract val type: String
}


@Serializable
data class AppEventActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val name: String,
    val payload: Map<String, String>?
) : ActionModel(), InAppAction, OnEventAction, PushAction, SilentPushAction

@Serializable
data class CustomEventActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val name: String,
    val payload: Map<String, String>?
) : ActionModel(), InAppAction, OnEventAction, PushAction

@Serializable
data class DismissActionModel(
    override val id: String,
    override val title: String,
    override val type: String
) : ActionModel(), PushAction, InAppAction

@Serializable
data class AskForPushPermissionActionModel(
    override val id: String,
    override val title: String,
    override val type: String
) : ActionModel(), InAppAction

@Serializable
data class OpenExternalUrlActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val url: String
) : ActionModel(), InAppAction, PushAction

@Serializable
data class BadgeCountActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val method: String,
    val value: Int
) : ActionModel(), PushAction


class UnknownActionModel : Action {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return true
    }

    override fun hashCode(): Int {
        return this::class.hashCode()
    }
}

sealed interface InAppAction
sealed interface PushAction
sealed interface SilentPushAction
sealed interface OnEventAction