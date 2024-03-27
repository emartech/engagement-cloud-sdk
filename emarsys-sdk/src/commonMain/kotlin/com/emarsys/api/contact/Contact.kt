package com.emarsys.api.contact

import Activatable
import com.emarsys.api.AutoRegisterable
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import kotlinx.coroutines.withContext

interface ContactInstance : ContactInternalApi, Activatable

interface ContactApi : ContactInternalApi, AutoRegisterable

class Contact<Logging : ContactInstance, Gatherer : ContactInstance, Internal : ContactInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(
    loggingApi,
    gathererApi,
    internalApi,
    sdkContext
), ContactApi {
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<ContactInternalApi>().linkContact(contactFieldId, contactFieldValue)
        }
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<ContactInternalApi>().linkAuthenticatedContact(contactFieldId, openIdToken)
        }
    }

    override suspend fun unlinkContact() {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<ContactInternalApi>().unlinkContact()
        }
    }

}