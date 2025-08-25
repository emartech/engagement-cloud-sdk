package com.emarsys.core.crypto

import com.emarsys.KotlinPlatform
import com.emarsys.core.exceptions.SdkException.DecryptionFailedException
import com.emarsys.core.log.Logger
import com.emarsys.currentPlatform
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.algorithms.SHA512
import dev.whyoleg.cryptography.operations.Hasher
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64

internal class Crypto(
    private val logger: Logger,
    private val publicKey: String
) : CryptoApi {
    private companion object {
        const val SECRET_START_INDEX = 0
        const val SECRET_END_INDEX = 32
    }

    private val aesGcm: AES.GCM = CryptographyProvider.Default.get(AES.GCM)
    private val hasher: Hasher = CryptographyProvider.Default.get(SHA512).hasher()
    private val ecdsaProvider = CryptographyProvider.Default.get(ECDSA)


    override suspend fun verify(message: String, signatureStr: String): Boolean {
        val decodedKey = ecdsaProvider.publicKeyDecoder(EC.Curve.P256)
            .decodeFromByteArray(EC.PublicKey.Format.DER, publicKey.decodeBase64Bytes())
        val (format: ECDSA.SignatureFormat, signature: ByteArray) = when (currentPlatform) {
            KotlinPlatform.JS -> {
                ECDSA.SignatureFormat.RAW to derToRawSignature(signatureStr.decodeBase64Bytes())
            }

            KotlinPlatform.Android -> {
                ECDSA.SignatureFormat.DER to signatureStr.decodeBase64Bytes()
            }

            KotlinPlatform.IOS -> {
                ECDSA.SignatureFormat.DER to signatureStr.decodeBase64Bytes()
            }
        }
        val verifier = decodedKey.signatureVerifier(SHA256, format)

        return verifier
            .tryVerifySignature(
                message.encodeToByteArray(),
                signature
            )
    }

    private suspend fun derToRawSignature(derSignature: ByteArray): ByteArray {
        val derSequenceHeaderToSkip = 2
        if (derSignature.size < 8 || derSignature[0] != 0x30.toByte()) {
            throw IllegalArgumentException("Invalid DER signature format")
        }

        var offset = derSequenceHeaderToSkip

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
            aesGcm.keyDecoder().decodeFromByteArray(
                AES.Key.Format.RAW,
                key.copyOfRange(SECRET_START_INDEX, SECRET_END_INDEX)
            )

        val encryptedValue = decodedKey.cipher().encrypt(value.encodeToByteArray())

        return encryptedValue.encodeBase64()
    }

    override suspend fun decrypt(encryptedValue: String, secret: String): String {
        val key = hasher.hash(secret.encodeToByteArray())
        val decrypted: ByteArray = try {
            val decodedKey: AES.GCM.Key =
                aesGcm.keyDecoder()
                    .decodeFromByteArray(
                        AES.Key.Format.RAW,
                        key.copyOfRange(SECRET_START_INDEX, SECRET_END_INDEX)
                    )

            decodedKey.cipher().decrypt(encryptedValue.decodeBase64Bytes())
        } catch (exception: Throwable) {
            throw DecryptionFailedException(exception.message ?: "Decryption failed")
        }

        return decrypted.decodeToString()
    }

}