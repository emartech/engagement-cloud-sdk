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
import kotlinx.datetime.Clock

class Crypto(private val aesGcm: AES.GCM, private val hasher: Hasher, private val publicKey: String) : CryptoApi {
    private companion object {
        const val SECRET_START_INDEX = 0
        const val SECRET_END_INDEX = 32
    }

    override suspend fun verify(message: String, signature: String): Boolean {
        val ecdsaProvider = CryptographyProvider.Default.get(ECDSA)

        val decodedKey = ecdsaProvider.publicKeyDecoder(EC.Curve.P256)
            .decodeFrom(EC.PublicKey.Format.DER, publicKey.decodeBase64Bytes())

        return decodedKey.signatureVerifier(SHA256, ECDSA.SignatureFormat.RAW)
            .verifySignature(message.encodeToByteArray(), prepareSignature(signature.decodeBase64Bytes()))
    }

    private fun prepareSignature(derSignature: ByteArray): ByteArray {
        if (derSignature.size < 8 || derSignature[0] != 0x30.toByte()) {
            throw IllegalArgumentException("Invalid DER signature format")
        }

        var offset = 2 // Skip the DER sequence header

        // Parse the first integer (r)
        if (derSignature[offset] != 0x02.toByte()) {
            throw IllegalArgumentException("Invalid DER signature format")
        }

        val rLength = derSignature[offset + 1].toInt()
        offset += 2

        val rBytes = ByteArray(32)
        val rByteStart = if (rLength > 32) offset + rLength - 32 else offset
        derSignature.copyInto(rBytes, 0, rByteStart, offset + rLength)
        offset += rLength

        // Parse the second integer (s)
        if (derSignature[offset] != 0x02.toByte()) {
            throw IllegalArgumentException("Invalid DER signature format")
        }

        val sLength = derSignature[offset + 1].toInt()
        offset += 2

        val sBytes = ByteArray(32)
        val sByteStart = if (sLength > 32) offset + sLength - 32 else offset
        derSignature.copyInto(sBytes, 0, sByteStart, offset + sLength)

        // Construct the RAW signature by concatenating r and s
        val rawSignature = ByteArray(64)
        rBytes.copyInto(rawSignature, 0)
        sBytes.copyInto(rawSignature, 32)

        return rawSignature
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
                aesGcm.keyDecoder()
                    .decodeFrom(AES.Key.Format.RAW, key.copyOfRange(SECRET_START_INDEX, SECRET_END_INDEX))

            decodedKey.cipher().decrypt(encryptedValue.decodeBase64Bytes())
        } catch (exception: Exception) {
            throw DecryptionFailedException(exception.message ?: "Decryption failed")
        }

        return decrypted?.decodeToString()
    }

}