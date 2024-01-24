package com.emarsys.core.crypto

import com.emarsys.core.exceptions.DecryptionFailedException
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDSA
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.symmetric.AES
import dev.whyoleg.cryptography.operations.hash.Hasher
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64

class Crypto(private val aesGcm: AES.GCM, private val hasher: Hasher, private val publicKey: String) : CryptoApi {
    private companion object {
        const val SECRET_START_INDEX = 0
        const val SECRET_END_INDEX = 32
    }

    override suspend fun verify(message: String, signature: String): Boolean {
        val ecdsaProvider = CryptographyProvider.Default.get(ECDSA)

        val decodedKey = ecdsaProvider.publicKeyDecoder(EC.Curve.P256)
            .decodeFrom(EC.PublicKey.Format.DER, publicKey.decodeBase64Bytes())

        return decodedKey.signatureVerifier(SHA256, ECDSA.SignatureFormat.DER)
            .verifySignature(message.encodeToByteArray(), signature.decodeBase64Bytes())
    }

    override suspend fun encrypt(value: String, secret: String): String {
        val key = hasher.hash(secret.encodeToByteArray())
        val decodedKey: AES.GCM.Key =
            aesGcm.keyDecoder().decodeFrom(AES.Key.Format.RAW, key.copyOfRange(SECRET_START_INDEX, SECRET_END_INDEX))

        val encryptedValue = decodedKey.cipher().encrypt(value.encodeToByteArray())

        return encryptedValue.encodeBase64()
    }

    override suspend fun decrypt(encryptedValue: String, secret: String): String? {
        val key = hasher.hash(secret.encodeToByteArray())
        val decrypted: ByteArray? = try {
            val decodedKey: AES.GCM.Key =
                aesGcm.keyDecoder().decodeFrom(AES.Key.Format.RAW, key.copyOfRange(SECRET_START_INDEX, SECRET_END_INDEX))

            decodedKey.cipher().decrypt(encryptedValue.decodeBase64Bytes())
        } catch (exception: Exception) {
            throw DecryptionFailedException(exception.message ?: "Decryption failed")
        }

        return decrypted?.decodeToString()
    }

}