package com.emarsys.api.embeddedmessaging

import com.emarsys.core.log.Logger
import com.emarsys.mobileengage.embeddedmessaging.ui.list.ListPageViewModelApi
import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory

internal class EmbeddedMessagingInternal(
    private val listPageViewModel: ListPageViewModelApi,
    private val sdkLogger: Logger,
) : EmbeddedMessagingInstance {
    override val categories: List<MessageCategory>
        get() = listPageViewModel.categories.value

    override suspend fun activate() {
        sdkLogger.debug("EmbeddedMessagingInternal - activate")
    }
}