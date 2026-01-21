package com.emarsys.api.contact


internal class JSContact(private val contactApi: ContactApi) :
    JSContactApi {

    /**
     * Links a contact to the SDK using the specified contact value.
     *
     * @param contactFieldValue The value of the contact field.
     * @return A promise that resolves when the contact is linked.
     */
    override suspend fun link(contactFieldValue: String) {
        contactApi.link(contactFieldValue).getOrThrow()
    }

    /**
     * Links an authenticated contact to the SDK using the OpenID token.
     * Authenticated contacts are already verified through any OpenID provider like Google or Apple
     *
     * @param openIdToken The OpenID token for authentication.
     */
    override suspend fun linkAuthenticated(openIdToken: String) {
        contactApi.linkAuthenticated(openIdToken).getOrThrow()
    }

    /**
     * Unlinks the currently linked contact from the SDK. And replaces it with an anonymous contact
     *
     * @return A promise that resolves when the contact is unlinked.
     */
    override suspend fun unlink() {
        contactApi.unlink().getOrThrow()
    }
}