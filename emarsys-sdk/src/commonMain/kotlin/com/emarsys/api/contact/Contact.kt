package com.emarsys.api.contact

import Activatable
import SdkContext
import com.emarsys.api.generic.GenericApi
import com.emarsys.core.exceptions.PreconditionFailedException
import kotlinx.coroutines.launch
import kotlin.reflect.typeOf

interface ContactInstance: ContactApi, Activatable

class Contact<Logging: ContactInstance, Gatherer: ContactInstance, Internal: ContactInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContext
): GenericApi<Logging, Gatherer, Internal>(loggingApi,
                                           gathererApi,
                                           internalApi,
                                           sdkContext), ContactApi {
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        sdkContext.sdkScope.launch {
            activeInstance<ContactApi>().linkContact(contactFieldId, contactFieldValue)
        }
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        sdkContext.sdkScope.launch {
            activeInstance<ContactApi>().linkAuthenticatedContact(contactFieldId, openIdToken)
        }
    }

    override suspend fun unlinkContact() {
        sdkContext.sdkScope.launch {
            activeInstance<ContactApi>().unlinkContact()
        }
    }

}