package com.sap.ec.mobileengage.embeddedmessaging.ui.item

import com.sap.ec.mobileengage.action.models.BasicRichContentDisplayActionModel
import com.sap.ec.networking.clients.embedded.messaging.model.Category
import io.ktor.http.Url

class MessageItemViewModel(
    private val model: MessageItemModelApi,
    override val isExcludedLocally: Boolean = false
) : MessageItemViewModelApi {

    override val id: String
        get() = model.message.id

    override val title: String
        get() = model.message.title

    override val lead: String
        get() = model.message.lead

    override val imageUrl: String?
        get() = model.message.listThumbnailImage?.src

    override val imageAltText: String?
        get() = model.message.listThumbnailImage?.alt

    override val categories: List<Category>
        get() = model.message.categories

    override val receivedAt: Long
        get() = model.message.receivedAt

    override val trackingInfo: String
        get() = model.message.trackingInfo

    override val isNotOpened: Boolean
        get() = model.isNotOpened()

    override val isPinned: Boolean
        get() = model.isPinned()

    override val isDeleted: Boolean
        get() = model.isDeleted()

    override val isRead: Boolean
        get() = model.isRead()

    override val richContentUrl: Url?
        get() {
            return getDefaultActionUrl()
        }

    override fun hasRichContent() = model.hasRichContent()

    override suspend fun fetchImage(): ByteArray =
        model.downloadImage()

    override suspend fun handleDefaultAction() {
        model.handleDefaultAction()
    }

    override suspend fun tagMessageOpened(): Result<Unit> {
        return model.tagMessageOpened()
    }

    override suspend fun deleteMessage(): Result<Unit> {
        return model.deleteMessage()
    }

    override suspend fun tagMessageRead(): Result<Unit> {
        return model.tagMessageRead()
    }

    override fun copyAsExcludedLocally(): MessageItemViewModelApi =
        MessageItemViewModel(model, isExcludedLocally = true)

    private fun getDefaultActionUrl(): Url? {
        val defaultAction = model.message.defaultAction
        return if (defaultAction is BasicRichContentDisplayActionModel) {
            Url(defaultAction.url)
            //TODO: SDK-576
        } else {
            null
        }
    }
}