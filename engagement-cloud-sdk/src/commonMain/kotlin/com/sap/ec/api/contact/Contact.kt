package com.sap.ec.api.contact

import com.sap.ec.api.Activatable
import com.sap.ec.api.generic.GenericApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.util.runCatchingWithoutCancellation
import kotlinx.coroutines.withContext

internal interface ContactInstance : ContactInternalApi, Activatable

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
        runCatchingWithoutCancellation {
            withContext(sdkContext.sdkDispatcher) {
                activeInstance<ContactInternalApi>().link(
                    contactFieldValue
                )
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
        runCatchingWithoutCancellation {
            withContext(sdkContext.sdkDispatcher) {
                activeInstance<ContactInternalApi>().linkAuthenticated(
                    openIdToken
                )
            }
        }

    /**
     * Unlinks the currently linked contact from the SDK. And replaces it with an anonymous contact
     */
    override suspend fun unlink(): Result<Unit> = runCatchingWithoutCancellation {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<ContactInternalApi>().unlink()
        }
    }
}