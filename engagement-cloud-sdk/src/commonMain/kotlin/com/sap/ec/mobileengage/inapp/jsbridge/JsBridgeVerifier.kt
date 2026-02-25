package com.sap.ec.mobileengage.inapp.jsbridge

import com.sap.ec.core.log.Logger
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.StringStorageApi
import com.sap.ec.networking.clients.jsbridge.JsBridgeClientApi

internal class JsBridgeVerifier(
    private val stringStorage: StringStorageApi,
    private val jsBridgeClient: JsBridgeClientApi,
    private val sdkLogger: Logger
) : JsBridgeVerifierApi {

    override suspend fun shouldInjectJsBridge(): Result<Boolean> {
        val serverMd5 = jsBridgeClient.fetchServerMd5().getOrElse {
            sdkLogger.error("JsBridge server MD5 fetch failed: ${it.message}")
            return Result.failure(it)
        }

        val cachedMd5 = stringStorage.get(StorageConstants.JS_BRIDGE_MD5_KEY)

        if (cachedMd5 != null && cachedMd5 == serverMd5) {
            return Result.success(true)
        }

        sdkLogger.debug("JsBridge MD5 mismatch or no cached MD5, re-downloading")
        jsBridgeClient.validateJSBridge().getOrElse {
            sdkLogger.error("JsBridge re-download failed: ${it.message}")
            return Result.failure(it)
        }

        return Result.success(true)
    }
}
