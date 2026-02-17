package com.sap.ec.mobileengage.embeddedmessaging.ui.item

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.util.DownloaderApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.mobileengage.action.ActionFactoryApi
import com.sap.ec.mobileengage.action.models.ActionModel
import com.sap.ec.mobileengage.action.models.BasicRichContentDisplayActionModel
import com.sap.ec.mobileengage.embeddedmessaging.models.MessageTagUpdate
import com.sap.ec.mobileengage.embeddedmessaging.models.TagOperation
import com.sap.ec.mobileengage.embeddedmessaging.ui.EmbeddedMessagingConstants.Image.BASE64_PLACEHOLDER_IMAGE
import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessage
import kotlin.io.encoding.Base64
import kotlin.time.ExperimentalTime

internal class MessageItemModel(
    override val message: EmbeddedMessage,
    private val downloaderApi: DownloaderApi,
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val actionFactory: ActionFactoryApi<ActionModel>,
    private val logger: Logger
) : MessageItemModelApi {

    private companion object {
        const val READ_TAG = "read"
        const val OPENED_TAG = "opened"
        const val PINNED_TAG = "pinned"
        const val DELETED_TAG = "deleted"
    }

    override suspend fun downloadImage(): ByteArray {
        return message.listThumbnailImage?.let {
            downloaderApi.download(it.src, getDecodedFallbackImage())
        } ?: getDecodedFallbackImage()
    }

    override suspend fun tagMessageOpened(): Result<Unit> {
        return updateTagsForMessage(OPENED_TAG, TagOperation.Add)
    }

    override suspend fun tagMessageRead(): Result<Unit> {
        return updateTagsForMessage(READ_TAG, TagOperation.Add)
    }

    override suspend fun deleteMessage(): Result<Unit> {
        return updateTagsForMessage(DELETED_TAG, TagOperation.Add)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun updateTagsForMessage(
        tag: String,
        operation: TagOperation
    ): Result<Unit> {
        logger.debug("Updating tag '$tag' with operation '$operation' for message id '${message.id}'")
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

            response.result
                .onFailure {
                    logger.debug(
                        "tag update failure: $tag $operation for message id '${message.id}'",
                        it
                    )
                }
                .map {
                    logger.debug("tag update success: $tag $operation for message id '${message.id}'")
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isNotOpened(): Boolean {
        return message.tags.map { it.lowercase() }.contains(OPENED_TAG).not()
    }

    override fun isRead(): Boolean {
        return message.tags.map { it.lowercase() }.contains(READ_TAG)
    }

    override fun isDeleted(): Boolean {
        return message.tags.map { it.lowercase() }.contains(DELETED_TAG)
    }

    override fun isPinned(): Boolean {
        return message.tags.map { it.lowercase() }.contains(PINNED_TAG)
    }

    override fun hasRichContent(): Boolean {
        return message.defaultAction is BasicRichContentDisplayActionModel
    }

    override suspend fun handleDefaultAction() {
        message.defaultAction?.let { defaultAction ->
            actionFactory.create(defaultAction).invoke()
        }
    }

    private fun getDecodedFallbackImage(): ByteArray {
        val byteArray = BASE64_PLACEHOLDER_IMAGE.encodeToByteArray()
        return Base64.decode(byteArray)
    }
}