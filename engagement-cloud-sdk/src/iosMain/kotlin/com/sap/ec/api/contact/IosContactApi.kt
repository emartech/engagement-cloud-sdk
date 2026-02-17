package com.sap.ec.api.contact

import io.ktor.utils.io.CancellationException

interface IosContactApi {

    @Throws(CancellationException::class)
    suspend fun link(contactFieldValue: String)

    @Throws(CancellationException::class)
    suspend fun linkAuthenticated(openIdToken: String)

    @Throws(CancellationException::class)
    suspend fun unlink()
}