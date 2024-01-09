package com.emarsys.api.contact

import Activatable
import SdkContext
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import com.emarsys.core.exceptions.PreconditionFailedException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.typeOf

interface ContactInstance : ContactApi, Activatable

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
            activeInstance<ContactApi>().linkContact(contactFieldId, contactFieldValue)
        }
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<ContactApi>().linkAuthenticatedContact(contactFieldId, openIdToken)
        }
    }

    override suspend fun unlinkContact() {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<ContactApi>().unlinkContact()
        }
    }

}