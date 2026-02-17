package com.sap.ec.api.contact

import Activatable
import com.sap.ec.api.generic.GenericApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.log.withLogContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

interface ContactInstance : ContactInternalApi, Activatable

internal class Contact<Logging : ContactInstance, Gatherer : ContactInstance, Internal : ContactInstance>(
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

    /**
     * Links a contact to the SDK using the specified contact field ID and value.
     *
     * @param contactFieldValue The value of the contact field.
     */
    override suspend fun link(contactFieldValue: String): Result<Unit> =
        runCatching {
            withContext(sdkContext.sdkDispatcher) {
                withLogContext(
                    buildJsonObject {
                        put(
                            "contactFieldValue", JsonPrimitive(contactFieldValue)
                        )
                    }) {
                    activeInstance<ContactInternalApi>().link(
                        contactFieldValue
                    )
                }
            }
        }

    /**
     * Links an authenticated contact to the SDK using the OpenID token.
     * Authenticated contacts are already verified through any OpenID provider like Google or Apple
     *
     * @param openIdToken The OpenID token for authentication.
     */
    override suspend fun linkAuthenticated(
        openIdToken: String
    ): Result<Unit> =
        runCatching {
            withContext(sdkContext.sdkDispatcher) {
                withLogContext(
                    buildJsonObject {
                        put(
                            "openIdToken", JsonPrimitive(openIdToken)
                        )
                    }) {
                    activeInstance<ContactInternalApi>().linkAuthenticated(
                        openIdToken
                    )
                }
            }
        }

    /**
     * Unlinks the currently linked contact from the SDK. And replaces it with an anonymous contact
     */
    override suspend fun unlink(): Result<Unit> = runCatching {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<ContactInternalApi>().unlink()
        }
    }
}