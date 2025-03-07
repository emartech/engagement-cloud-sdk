package com.emarsys.api.contact

import Activatable
import com.emarsys.api.generic.GenericApi
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.withLogContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

interface ContactInstance : ContactInternalApi, Activatable

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
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String): Result<Unit> =
        runCatching {
            withContext(sdkContext.sdkDispatcher) {
                withLogContext(
                    buildJsonObject {
                        put(
                            "contactFieldId", JsonPrimitive(contactFieldId)
                        )
                        put(
                            "contactFieldValue", JsonPrimitive(contactFieldValue)
                        )
                    }) {
                    activeInstance<ContactInternalApi>().linkContact(
                        contactFieldId,
                        contactFieldValue
                    )
                }
            }
        }

    override suspend fun linkAuthenticatedContact(
        contactFieldId: Int,
        openIdToken: String
    ): Result<Unit> =
        runCatching {
            withContext(sdkContext.sdkDispatcher) {
                withLogContext(
                    buildJsonObject {
                        put(
                            "contactFieldId", JsonPrimitive(contactFieldId)
                        )
                        put(
                            "openIdToken", JsonPrimitive(openIdToken)
                        )
                    }) {
                    activeInstance<ContactInternalApi>().linkAuthenticatedContact(
                        contactFieldId,
                        openIdToken
                    )
                }
            }
        }

    override suspend fun unlinkContact(): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<ContactInternalApi>().unlinkContact()
        }
    }
}