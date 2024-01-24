package com.emarsys.core.crypto

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.asymmetric.EC
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDSA
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import io.ktor.util.decodeBase64Bytes

class Crypto(private val publicKey: String) : CryptoApi {
    override suspend fun verify(message: String, signature: String): Boolean {
        val ecdsaProvider = CryptographyProvider.Default.get(ECDSA)

        val decodedKey = ecdsaProvider.publicKeyDecoder(EC.Curve.P256)
            .decodeFrom(EC.PublicKey.Format.DER, publicKey.decodeBase64Bytes())

        return decodedKey.signatureVerifier(SHA256, ECDSA.SignatureFormat.DER)
            .verifySignature(message.encodeToByteArray(), signature.decodeBase64Bytes())
    }

    override fun encrypt() {
        TODO("Not yet implemented")
    }

    override fun decrypt(): String? {
        TODO("Not yet implemented")
    }


}