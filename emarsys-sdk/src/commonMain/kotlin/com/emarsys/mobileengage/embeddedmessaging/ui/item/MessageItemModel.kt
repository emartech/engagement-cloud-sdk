package com.emarsys.mobileengage.embeddedmessaging.ui.item

import com.emarsys.core.channel.SdkEventDistributorApi
import com.emarsys.core.networking.model.Response
import com.emarsys.core.util.DownloaderApi
import com.emarsys.event.SdkEvent
import com.emarsys.mobileengage.action.ActionFactoryApi
import com.emarsys.mobileengage.action.models.ActionModel
import com.emarsys.mobileengage.embeddedmessaging.models.MessageTagUpdate
import com.emarsys.mobileengage.embeddedmessaging.models.TagOperation
import com.emarsys.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Image.BASE64_PLACEHOLDER_IMAGE
import com.emarsys.networking.clients.embedded.messaging.model.EmbeddedMessage
import kotlin.io.encoding.Base64
import kotlin.time.ExperimentalTime

internal class MessageItemModel(
    override val message: EmbeddedMessage,
    private val downloaderApi: DownloaderApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val actionFactory: ActionFactoryApi<ActionModel>
) : MessageItemModelApi {

    private companion object {
        const val UNREAD_TAG = "unread"
        const val PINNED_TAG = "pinned"
    }
    override suspend fun downloadImage(): ByteArray {
        return message.listThumbnailImage?.let {
            downloaderApi.download(it.src, getDecodedFallbackImage())
        } ?: getDecodedFallbackImage()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun updateTagsForMessage(
        tag: String,
        operation: TagOperation
    ): Boolean {
        return try {
            val updateData = listOf(
                MessageTagUpdate(
                    messageId = message.id,
                    operation = operation,
                    tag = tag,
                    trackingInfo = message.trackingInfo
                )
            )
            val updateTagsEvent = SdkEvent.Internal.EmbeddedMessaging.UpdateTagsForMessages(
                nackCount = 0,
                updateData = updateData
            )
            val waiter = sdkEventDistributor.registerEvent(updateTagsEvent)
            val response = waiter.await<Response>()

            response.result.fold(
                onSuccess = { true },
                onFailure = { false }
            )
        } catch (e: Exception) {
            false
        }
    }

    override fun isUnread(): Boolean {
        return message.tags.map { it.lowercase() }.contains(UNREAD_TAG)
    }

    override fun isPinned(): Boolean {
        return message.tags.map { it.lowercase() }.contains(PINNED_TAG)
    }

    override fun hasDefaultAction(): Boolean {
        return message.defaultAction != null
    }

    override suspend fun handleDefaultAction() {
        message.defaultAction?.let {
            actionFactory.create(it).invoke()
        }
    }

    private fun getDecodedFallbackImage(): ByteArray {
        val byteArray = BASE64_PLACEHOLDER_IMAGE.encodeToByteArray()
        return Base64.decode(byteArray)
    }
}