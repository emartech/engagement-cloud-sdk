package com.emarsys.api.contact

import com.emarsys.api.AutoRegisterable

interface ContactApi: AutoRegisterable {
    suspend fun linkContact(contactFieldId: Int, contactFieldValue: String): Result<Unit>

    suspend fun linkAuthenticatedContact(contactFieldId: Int, openIdToken: String): Result<Unit>

    suspend fun unlinkContact(): Result<Unit>
}