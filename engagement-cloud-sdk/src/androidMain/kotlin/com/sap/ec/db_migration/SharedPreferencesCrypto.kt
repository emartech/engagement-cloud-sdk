package com.sap.ec.db_migration


import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.sap.ec.core.log.Logger
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SharedPreferenceCrypto(
    private val sdkLogger: Logger
) {
    companion object {
        const val KEYSTORE_ALIAS = "emarsys_sdk_key_shared_pref_key_v3"
    }

    private var secretKey: SecretKey = getOrCreateSecretKey()

    suspend fun decrypt(value: String): String? {
        return try {
            val ivBytes = Base64.decode(value.substring(0, 16), Base64.DEFAULT)
            val encryptedBytes = Base64.decode(value.substring(16), Base64.DEFAULT)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, ivBytes))
            val decrypted = cipher.doFinal(encryptedBytes)
            String(decrypted)
        } catch (exception: GeneralSecurityException) {
            logCryptoError(value, exception)
            secretKey = createSecretKey()
            null
        } catch (exception: IllegalArgumentException) {
            logCryptoError(value, exception)
            if (exception.message?.contains("bad base-64") == true) {
                value
            } else {
                null
            }
        } catch (exception: Exception) {
            logCryptoError(value, exception)
            null
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            return createSecretKey()
        }

        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    private fun createSecretKey(): SecretKey {
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    private suspend fun logCryptoError(value: String, exception: Exception) {
        sdkLogger.debug("Failed to decrypt value: $value", exception)
    }
}