package com.emarsys.core.exceptions

import com.emarsys.core.networking.model.Response

sealed class SdkException(message: String) : RuntimeException(message) {
    class InvalidApplicationCodeException(message: String): SdkException(message)
    class SdkAlreadyEnabledException(message: String): SdkException(message)
    class SdkAlreadyDisabledException(message: String): SdkException(message)
    class RetryLimitReachedException(message: String, val response: Response) : SdkException(message)
    class PreconditionFailedException(message: String): SdkException(message)
    class MissingApplicationCodeException(message: String): SdkException(message)
    class FailedRequestException(val response: Response) : SdkException("request: ${response.originalRequest.url}, responseBody: ${response.bodyAsText}")
    class NetworkIOException(message: String): SdkException(message)
    class CoroutineException(message: String): SdkException(message)
    class DecryptionFailedException(message: String): SdkException(message)
}
