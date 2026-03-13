package com.sap.ec.api.embeddedmessaging

import com.sap.ec.api.Activatable
import com.sap.ec.api.generic.GenericApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.networking.clients.embedded.messaging.model.Category

internal interface EmbeddedMessagingInstance : EmbeddedMessagingInternalApi, Activatable

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

    override val categories: List<Category>
        get() = activeInstance<EmbeddedMessagingInternalApi>().categories
    override val isUnreadFilterActive: Boolean
        get() = activeInstance<EmbeddedMessagingInternalApi>().isUnreadFilterActive
    override val activeCategoryFilters: List<Category>
        get() = activeInstance<EmbeddedMessagingInternalApi>().activeCategoryFilters.toList()

    override fun filterUnreadOnly(filterUnreadOnly: Boolean) {
        activeInstance<EmbeddedMessagingInternalApi>().filterUnreadOnly(filterUnreadOnly)
    }

    override fun filterByCategories(categories: List<Category>) {
        activeInstance<EmbeddedMessagingInternalApi>().filterByCategories(categories.toSet())
    }
}