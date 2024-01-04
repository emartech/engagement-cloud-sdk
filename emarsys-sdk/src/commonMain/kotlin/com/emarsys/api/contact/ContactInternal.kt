package com.emarsys.api.contact

class ContactInternal: ContactInstance {
    override suspend fun linkContact(contactFieldId: Int, contactFieldValue: String) {
        TODO("Not yet implemented")
    }

    override suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String) {
        TODO("Not yet implemented")
    }

    override suspend fun unlinkContact() {
        TODO("Not yet implemented")
    }

    override suspend fun activate() {
    }

}