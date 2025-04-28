package com.emarsys.api.contact

import io.ktor.utils.io.CancellationException

interface IosPublicContactApi {

    @Throws(CancellationException::class)
    suspend fun link(contactFieldId: Int, contactFieldValue: String)

    @Throws(CancellationException::class)
    suspend fun linkAuthenticated(contactFieldId: Int, openIdToken: String)

    @Throws(CancellationException::class)
    suspend fun unlink()
}