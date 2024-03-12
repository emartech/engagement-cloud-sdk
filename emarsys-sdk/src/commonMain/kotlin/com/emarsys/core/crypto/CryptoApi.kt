package com.emarsys.core.crypto

interface CryptoApi {

    suspend fun verify(message: String, signatureStr: String): Boolean

    suspend fun encrypt(value: String, secret: String): String

    suspend fun decrypt(encryptedValue: String, secret: String): String?
}