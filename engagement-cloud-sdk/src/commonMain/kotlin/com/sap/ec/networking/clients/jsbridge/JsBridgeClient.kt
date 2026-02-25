package com.sap.ec.networking.clients.jsbridge

import com.sap.ec.context.Features
import com.sap.ec.context.SdkContextApi
import com.sap.ec.core.crypto.CryptoApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.networking.clients.NetworkClientApi
import com.sap.ec.core.networking.model.Response
import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.StringStorageApi
import io.ktor.http.HttpMethod
import io.ktor.http.Url

internal class JsBridgeClient(
    private val networkClient: NetworkClientApi,
    private val crypto: CryptoApi,
    private val sdkContext: SdkContextApi,
    private val stringStorage: StringStorageApi,
    private val sdkLogger: Logger
) : JsBridgeClientApi {

    override suspend fun validateJSBridge(): Result<Unit> {
        val jsResponse = fetchResponse(sdkContext.defaultUrls.jsBridgeUrl).getOrElse {
            sdkLogger.error("Failed to fetch JsBridge: ${it.message}")
            return Result.failure(it)
        }

        if (isFeatureEnabled()) {
            val signatureResponse = fetchResponse(sdkContext.defaultUrls.jsBridgeSignatureUrl).getOrElse {
                sdkLogger.error("Failed to fetch JsBridge signature: ${it.message}")
                return Result.failure(it)
            }

            val verified = runCatching {
                crypto.verify(jsResponse.bodyAsText, signatureResponse.bodyAsText)
            }.getOrElse {
                sdkLogger.error("JsBridge crypto verification error: ${it.message}")
                return Result.failure(it)
            }

            if (!verified) {
                sdkLogger.error("JsBridge signature verification failed")
                stringStorage.put(StorageConstants.JS_BRIDGE_MD5_KEY, null)
                return Result.failure(IllegalStateException("JsBridge signature verification failed"))
            }
        }

        val md5 = parseMd5FromGoogHash(jsResponse.headers)
        stringStorage.put(StorageConstants.JS_BRIDGE_MD5_KEY, md5)
        sdkLogger.debug("JsBridge fetched and hash cached")
        return Result.success(Unit)
    }

    override suspend fun fetchServerMd5(): Result<String> {
        val request = UrlRequest(
            url = Url(sdkContext.defaultUrls.jsBridgeUrl),
            method = HttpMethod.Head
        )
        val response = networkClient.send(request).getOrElse {
            sdkLogger.error("JsBridge HEAD request failed: ${it.message}")
            return Result.failure(it)
        }
        val md5 = parseMd5FromGoogHash(response.headers)
        if (md5 == null) {
            sdkLogger.error("JsBridge HEAD response missing x-goog-hash MD5")
            return Result.failure(IllegalStateException("JsBridge HEAD response missing x-goog-hash MD5"))
        }
        return Result.success(md5)
    }

    private fun isFeatureEnabled(): Boolean {
        return sdkContext.features.contains(Features.JsBridgeSignatureCheck)
    }

    private suspend fun fetchResponse(url: String): Result<Response> {
        val request = UrlRequest(
            url = Url(url),
            method = HttpMethod.Get
        )
        return networkClient.send(request)
    }
}
