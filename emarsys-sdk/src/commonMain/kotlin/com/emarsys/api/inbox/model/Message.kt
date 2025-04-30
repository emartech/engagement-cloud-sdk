package com.emarsys.api.inbox.model

import com.emarsys.mobileengage.action.models.ActionModel
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Message(
    val id: String,
    val campaignId: String,
    val collapseId: String? = null,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val receivedAt: Long,
    val updatedAt: Long? = null,
    val expiresAt: Long? = null,
    val tags: List<String>? = null,
    val properties: Map<String, String>? = null,
    val actions: List<ActionModel>? = null
)