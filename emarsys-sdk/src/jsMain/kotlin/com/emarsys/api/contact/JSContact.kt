package com.emarsys.api.contact

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

class JSContact(private val contactApi: ContactApi, private val applicationScope: CoroutineScope) : JSContactApi {

    /**
     * Links a contact to the SDK using the specified contact field ID and value.
     *
     * @param contactFieldId The ID of the contact field.
     * @param contactFieldValue The value of the contact field.
     * @return A promise that resolves when the contact is linked.
     */
    override fun link(contactFieldId: Int, contactFieldValue: String): Promise<Unit> {
        return applicationScope.promise {
            contactApi.linkContact(contactFieldId, contactFieldValue).getOrThrow()
        }
    }

    /**
     * Links an authenticated contact to the SDK using the specified contact field ID and OpenID token.
     * Authenticated contacts are already verified through any OpenID provider like Google or Apple
     *
     * @param contactFieldId The ID of the contact field.
     * @param openIdToken The OpenID token for authentication.
     */
    override fun linkAuthenticated(contactFieldId: Int, openIdToken: String): Promise<Unit> {
        return applicationScope.promise {
            contactApi.linkAuthenticatedContact(contactFieldId, openIdToken).getOrThrow()
        }
    }

    /**
     * Unlinks the currently linked contact from the SDK. And replaces it with an anonymous contact
     *
     * @return A promise that resolves when the contact is unlinked.
     */
    override fun unlink(): Promise<Unit> {
        return applicationScope.promise {
            contactApi.unlinkContact().getOrThrow()
        }
    }
}