package com.emarsys.core.crypto

interface CryptoApi {

    suspend fun verify(message: String, signature: String): Boolean

    fun encrypt()

    fun decrypt(): String?
}