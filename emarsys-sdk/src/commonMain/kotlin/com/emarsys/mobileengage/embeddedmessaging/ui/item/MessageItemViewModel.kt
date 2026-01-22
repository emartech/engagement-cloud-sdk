package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.mobileengage.action.models.BasicRichContentDisplayActionModel
import com.emarsys.networking.clients.embedded.messaging.model.Category
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

    override val isUnread: Boolean
        get() = model.isUnread()

    override val isPinned: Boolean
        get() = model.isPinned()

    override val isDeleted: Boolean
        get() = model.isDeleted()

    override val richContentUrl: Url?
        get() {
            return getDefaultActionUrl()
        }

    override fun shouldNavigate() = model.shouldNavigate()

    override suspend fun fetchImage(): ByteArray =
        model.downloadImage()

    override suspend fun handleDefaultAction() {
        model.handleDefaultAction()
    }

    override suspend fun tagMessageRead(): Result<Unit> {
        return model.tagMessageRead()
    }

    override suspend fun deleteMessage(): Result<Unit> {
        return model.deleteMessage()
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