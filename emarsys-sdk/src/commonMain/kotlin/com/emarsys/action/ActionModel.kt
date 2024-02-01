package com.emarsys.action

import kotlinx.serialization.Serializable

@Serializable
sealed class ActionModel {
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
) : ActionModel(), InAppAction, OnEventAction

@Serializable
data class CustomEventActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val name: String,
    val payload: Map<String, String>?
) : ActionModel(), InAppAction, OnEventAction

@Serializable
data class DismissActionModel(
    override val id: String,
    override val title: String,
    override val type: String
) : ActionModel()

@Serializable
data class OpenExternalUrlActionModel(
    override val id: String,
    override val title: String,
    override val type: String,
    val url: String
) : ActionModel(), InAppAction

sealed interface InAppAction {}
sealed interface OnEventAction {}