package com.sap.ec.api.embeddedmessaging

import Activatable
import com.sap.ec.api.generic.GenericApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

interface EmbeddedMessagingInstance : EmbeddedMessagingInternalApi, Activatable

internal class EmbeddedMessaging<Logging : EmbeddedMessagingInstance, Gatherer : EmbeddedMessagingInstance, Internal : EmbeddedMessagingInstance>(
    logging: Logging,
    gatherer: Gatherer,
    internal: Internal,
    sdkContext: SdkContextApi,
) : GenericApi<Logging, Gatherer, Internal>(
    logging,
    gatherer,
    internal,
    sdkContext
), EmbeddedMessagingApi {

    override val categories: List<MessageCategory>
        get() = activeInstance<EmbeddedMessagingInternalApi>().categories
    override val isUnreadFilterActive: Boolean
        get() = activeInstance<EmbeddedMessagingInternalApi>().isUnreadFilterActive
    override val activeCategoryIdFilters: List<Int>
        get() = activeInstance<EmbeddedMessagingInternalApi>().activeCategoryIdFilters.toList()

    override fun filterUnreadOnly(filterUnreadOnly: Boolean) {
        activeInstance<EmbeddedMessagingInternalApi>().filterUnreadOnly(filterUnreadOnly)
    }

    override fun filterByCategoryIds(categoryIds: List<Int>) {
        activeInstance<EmbeddedMessagingInternalApi>().filterByCategoryIds(categoryIds.toSet())
    }

}